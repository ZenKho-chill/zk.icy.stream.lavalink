package com.zenkho.icy.source;

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

    public IcySourceManager(Config config) {
        this.config = config;
        this.httpClient = createHttpClient();
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
                    .header("User-Agent", "Lavalink ICY Stream Plugin/1.0")
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
        
        if (!isStreamUrl(identifier)) {
            return null;
        }

        try {
            return loadStream(identifier);
        } catch (Exception e) {
            log.error("Failed to load stream: {}", identifier, e);
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
        try (Response response = httpClient.newCall(
            new Request.Builder()
                .url(url)
                .head()
                .build()
        ).execute()) {
            
            String contentType = response.header("Content-Type");
            if (contentType != null) {
                for (String supportedType : SUPPORTED_CONTENT_TYPES) {
                    if (contentType.toLowerCase().contains(supportedType)) {
                        return true;
                    }
                }
            }
            
            // Check for ICY protocol
            String icyName = response.header("icy-name");
            return icyName != null;
            
        } catch (Exception e) {
            log.debug("Failed to probe URL: {}", url, e);
            return false;
        }
    }

    private AudioItem loadStream(String url) throws IOException {
        Request request = new Request.Builder()
            .url(url)
            .header("Icy-MetaData", "1")
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            String title = response.header("icy-name");
            if (title == null || title.isEmpty()) {
                title = "Radio Stream";
            }

            String genre = response.header("icy-genre");
            String bitrate = response.header("icy-br");
            
            String author = genre != null ? genre : "Unknown";
            
            AudioTrackInfo trackInfo = new AudioTrackInfo(
                title,
                author,
                Long.MAX_VALUE, // Infinite duration for live streams
                url,
                true, // isStream
                url
            );

            log.info("Loaded stream: {} - {} ({}kbps)", title, author, bitrate);
            
            return new IcyStreamAudioTrack(trackInfo, this, config);
        }
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
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

