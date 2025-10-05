package com.zenkho.icy.source;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.zenkho.icy.IcyStreamPlugin.Config;
import com.zenkho.icy.stream.IcyStreamAudioTrack;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class IcySourceManager implements AudioSourceManager {

    private static final Logger log = LoggerFactory.getLogger(IcySourceManager.class);
    
    private static final Pattern STREAM_URL_PATTERN = Pattern.compile(
        "^https?://.*\\.(mp3|aac|aacp|m3u8|pls)(?:\\?.*)?$|" +
        "^https?://.*(?:radio|stream|live|broadcast).*$",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final String[] SUPPORTED_CONTENT_TYPES = {
        "audio/mpeg",
        "audio/aac",
        "audio/aacp",
        "audio/x-mpegurl",
        "application/vnd.apple.mpegurl",
        "audio/mpegurl",
        "audio/x-scpls"
    };

    private final Config config;
    private final OkHttpClient httpClient;
    private final MediaContainerRegistry containerRegistry;

    public IcySourceManager(Config config) {
        this.config = config;
        this.httpClient = createHttpClient();
        this.containerRegistry = MediaContainerRegistry.DEFAULT_REGISTRY;
        log.info("IcySourceManager initialized");
    }

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(config.getConnectionTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
            .followRedirects(config.isFollowRedirects())
            .followSslRedirects(config.isFollowRedirects())
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                    .header("Icy-MetaData", "1")
                    .header("User-Agent", "Lavalink ICY Stream Plugin/1.1.0")
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "identity") // Disable compression to avoid header issues
                    .header("Connection", "close") // Avoid keep-alive issues
                    .build();
                return chain.proceed(request);
            })
            .build();
    }

    @Override
    public String getSourceName() {
        return "icy-stream";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        String identifier = reference.identifier;
        
        log.debug("Attempting to load item: {}", identifier);
        
        if (!isStreamUrl(identifier)) {
            log.debug("URL {} is not identified as a stream URL", identifier);
            return null;
        }

        try {
            log.debug("Loading stream from URL: {}", identifier);
            AudioItem result = loadStream(identifier);
            log.debug("Successfully loaded stream: {}", identifier);
            return result;
        } catch (IOException e) {
            log.error("IOException while loading stream: {} - {}", identifier, e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.error("Invalid URL format: {} - {}", identifier, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error loading stream: {}", identifier, e);
            return null;
        }
    }

    private boolean isStreamUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        // Check URL pattern
        if (STREAM_URL_PATTERN.matcher(url).matches()) {
            return true;
        }
        
        // Optionally probe the URL to check content type
        Request headRequest = new Request.Builder()
            .url(url)
            .head()
            .header("User-Agent", "Lavalink ICY Stream Plugin/1.1.0")
            .header("Accept", "*/*")
            .build();
            
        try (Response response = httpClient.newCall(headRequest).execute()) {
            
            if (!response.isSuccessful()) {
                log.debug("HEAD request failed for URL: {} with code: {}", url, response.code());
                return false;
            }
            
            String contentType = response.header("Content-Type");
            if (contentType != null) {
                String lowerContentType = contentType.toLowerCase();
                for (String supportedType : SUPPORTED_CONTENT_TYPES) {
                    if (lowerContentType.contains(supportedType)) {
                        log.debug("URL {} identified as stream by content-type: {}", url, contentType);
                        return true;
                    }
                }
            }
            
            // Check for ICY protocol
            String icyName = response.header("icy-name");
            String icyGenre = response.header("icy-genre");
            boolean isIcyStream = icyName != null || icyGenre != null;
            
            if (isIcyStream) {
                log.debug("URL {} identified as ICY stream", url);
            }
            
            return isIcyStream;
            
        } catch (Exception e) {
            log.debug("Failed to probe URL: {} - {}", url, e.getMessage());
            // If we can't probe, let's be conservative and not assume it's a stream
            return false;
        }
    }

    private AudioItem loadStream(String url) throws IOException {
        Request request = new Request.Builder()
            .url(url)
            .header("Icy-MetaData", "1")
            .header("Accept", "*/*")
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code() + " for URL: " + url);
            }

            // Safely extract headers with null checks and sanitization
            String title = sanitizeHeader(response.header("icy-name"));
            if (title == null || title.isEmpty()) {
                title = "Radio Stream";
            }

            String genre = sanitizeHeader(response.header("icy-genre"));
            String bitrate = sanitizeHeader(response.header("icy-br"));
            
            String author = (genre != null && !genre.isEmpty()) ? genre : "Unknown";
            
            AudioTrackInfo trackInfo = new AudioTrackInfo(
                title,
                author,
                Long.MAX_VALUE, // Infinite duration for live streams
                url,
                true, // isStream
                url
            );

            log.info("Loaded stream: {} - {} ({}kbps)", title, author, bitrate != null ? bitrate : "Unknown");
            
            return new IcyStreamAudioTrack(trackInfo, this, config);
        } catch (Exception e) {
            log.error("Failed to load stream from URL: {}", url, e);
            throw new IOException("Failed to load stream: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sanitize header values to prevent issues with special characters
     */
    private String sanitizeHeader(String headerValue) {
        if (headerValue == null) {
            return null;
        }
        
        // Remove any control characters and normalize
        return headerValue.replaceAll("[\\p{Cntrl}]", "")
                         .trim()
                         .replaceAll("\\s+", " "); // Replace multiple spaces with single space
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public MediaContainerRegistry getMediaContainerRegistry() {
        return containerRegistry;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // Not needed for streams
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new IcyStreamAudioTrack(trackInfo, this, config);
    }

    @Override
    public void shutdown() {
        // Cleanup if needed
        log.info("IcySourceManager shutdown");
    }
}

