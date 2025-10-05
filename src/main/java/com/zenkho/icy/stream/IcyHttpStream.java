package com.zenkho.icy.stream;

import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.zenkho.icy.IcyStreamPlugin.Config;
import com.zenkho.icy.metadata.IcyMetadataParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class IcyHttpStream implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(IcyHttpStream.class);

    private final String url;
    private final OkHttpClient httpClient;
    private final Config config;
    private Response response;
    private IcyStreamInputStream inputStream;
    private int icyMetaInt;
    private String streamTitle;
    private int reconnectAttempts;

    public IcyHttpStream(String url, OkHttpClient httpClient, Config config) throws IOException {
        this.url = url;
        this.httpClient = httpClient;
        this.config = config;
        this.reconnectAttempts = 0;
        connect();
    }

    private void connect() throws IOException {
        try {
            Request request = new Request.Builder()
                .url(url)
                .header("Icy-MetaData", "1")
                .header("User-Agent", "Lavalink ICY Stream Plugin/1.1.1")
                .header("Accept", "*/*")
                .header("Connection", "close")
                .build();

            response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                String errorMsg = String.format("HTTP %d: %s for URL: %s", 
                    response.code(), response.message(), url);
                response.close();
                throw new IOException(errorMsg);
            }

            // Parse ICY headers with better error handling
            String icyMetaIntStr = response.header("icy-metaint");
            if (icyMetaIntStr != null && !icyMetaIntStr.isEmpty()) {
                try {
                    icyMetaInt = Integer.parseInt(icyMetaIntStr.trim());
                    if (icyMetaInt < 0) {
                        log.warn("Negative icy-metaint header: {}, setting to 0", icyMetaIntStr);
                        icyMetaInt = 0;
                    } else {
                        log.info("ICY metadata interval: {}", icyMetaInt);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid icy-metaint header: {}, setting to 0", icyMetaIntStr);
                    icyMetaInt = 0;
                }
            } else {
                icyMetaInt = 0;
            }

            // Safely extract ICY headers
            String icyName = sanitizeHeader(response.header("icy-name"));
            String icyGenre = sanitizeHeader(response.header("icy-genre"));
            String icyBr = sanitizeHeader(response.header("icy-br"));

            log.info("Connected to stream - Name: {}, Genre: {}, Bitrate: {}kbps", 
                icyName != null ? icyName : "Unknown", 
                icyGenre != null ? icyGenre : "Unknown", 
                icyBr != null ? icyBr : "Unknown");

            ResponseBody body = response.body();
            if (body == null) {
                response.close();
                throw new IOException("Response body is null for URL: " + url);
            }

            InputStream rawStream = body.byteStream();
            
            if (icyMetaInt > 0 && config.isEnableMetadata()) {
                inputStream = new IcyMetadataInputStream(rawStream, icyMetaInt);
            } else {
                inputStream = new IcyStreamInputStream(rawStream);
            }
            
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid URL: " + url, e);
        } catch (IOException e) {
            throw e; // Re-throw IOException as is
        } catch (Exception e) {
            throw new IOException("Unexpected error connecting to stream: " + url, e);
        }
    }
    
    /**
     * Sanitize header values to prevent issues with special characters
     */
    private String sanitizeHeader(String headerValue) {
        if (headerValue == null || headerValue.trim().isEmpty()) {
            return null;
        }
        
        // Remove any control characters and normalize
        return headerValue.replaceAll("[\\p{Cntrl}]", "")
                         .trim()
                         .replaceAll("\\s+", " "); // Replace multiple spaces with single space
    }

    public SeekableInputStream getInputStream() {
        return new NonSeekableInputStream(inputStream);
    }

    private void reconnect() throws IOException {
        if (!config.isAutoReconnect()) {
            throw new IOException("Stream connection lost and auto-reconnect is disabled");
        }

        if (reconnectAttempts >= config.getMaxRetries()) {
            throw new IOException("Max reconnection attempts reached: " + reconnectAttempts);
        }

        reconnectAttempts++;
        log.info("Reconnecting to stream (attempt {}/{})", reconnectAttempts, config.getMaxRetries());

        try {
            Thread.sleep(config.getRetryDelay());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Reconnection interrupted", e);
        }

        close();
        connect();
        reconnectAttempts = 0; // Reset on successful reconnection
        log.info("Successfully reconnected to stream");
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (response != null) {
            response.close();
        }
    }

    // Base input stream wrapper
    private static class IcyStreamInputStream extends InputStream {
        protected final InputStream delegate;

        IcyStreamInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }
    }

    // Input stream that handles ICY metadata
    private class IcyMetadataInputStream extends IcyStreamInputStream {
        private final int metaInt;
        private int bytesUntilMetadata;
        private final IcyMetadataParser metadataParser;

        IcyMetadataInputStream(InputStream delegate, int metaInt) {
            super(delegate);
            this.metaInt = metaInt;
            this.bytesUntilMetadata = metaInt;
            this.metadataParser = new IcyMetadataParser();
        }

        @Override
        public int read() throws IOException {
            if (bytesUntilMetadata == 0) {
                readMetadata();
                bytesUntilMetadata = metaInt;
            }
            
            bytesUntilMetadata--;
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (bytesUntilMetadata == 0) {
                readMetadata();
                bytesUntilMetadata = metaInt;
            }

            int toRead = Math.min(len, bytesUntilMetadata);
            int bytesRead = delegate.read(b, off, toRead);
            
            if (bytesRead > 0) {
                bytesUntilMetadata -= bytesRead;
            }
            
            return bytesRead;
        }

        private void readMetadata() throws IOException {
            try {
                int size = delegate.read();
                if (size < 0) {
                    throw new IOException("Unexpected end of stream while reading metadata size");
                }

                int metadataLength = size * 16;
                if (metadataLength == 0) {
                    return; // No metadata to read
                }
                
                // Sanity check for metadata length
                if (metadataLength > 16 * 255) { // Maximum possible metadata length
                    log.warn("Metadata length seems too large: {} bytes, skipping", metadataLength);
                    delegate.skip(metadataLength);
                    return;
                }

                byte[] metadataBytes = new byte[metadataLength];
                int totalBytesRead = 0;
                
                while (totalBytesRead < metadataLength) {
                    int result = delegate.read(metadataBytes, totalBytesRead, metadataLength - totalBytesRead);
                    if (result < 0) {
                        throw new IOException("Unexpected end of stream while reading metadata");
                    }
                    totalBytesRead += result;
                }

                // Process metadata with proper encoding handling
                String metadata = new String(metadataBytes, "UTF-8").trim();
                
                // Remove null bytes that might be present in metadata
                metadata = metadata.replaceAll("\0", "").trim();
                
                if (!metadata.isEmpty()) {
                    String title = metadataParser.parseStreamTitle(metadata);
                    
                    if (title != null && !title.equals(streamTitle)) {
                        streamTitle = title;
                        log.info("Now playing: {}", streamTitle);
                        // TODO: Emit metadata update event to Lavalink
                    }
                }
                
            } catch (IOException e) {
                log.error("Error reading ICY metadata, continuing without metadata: {}", e.getMessage());
                // Don't throw the exception, just log it and continue
            } catch (Exception e) {
                log.error("Unexpected error processing ICY metadata: {}", e.getMessage(), e);
                // Don't throw the exception, just log it and continue
            }
        }
    }

    // SeekableInputStream adapter (non-seekable)
    private static class NonSeekableInputStream extends SeekableInputStream {
        private final InputStream delegate;

        NonSeekableInputStream(InputStream delegate) {
            super(0, Long.MAX_VALUE);
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public long getPosition() {
            return 0;
        }

        @Override
        public boolean canSeekHard() {
            return false;
        }

        @Override
        protected void seekHard(long position) throws IOException {
            throw new UnsupportedOperationException("Stream is not seekable");
        }

        @Override
        public java.util.List<com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoProvider> getTrackInfoProviders() {
            return java.util.Collections.emptyList();
        }
    }
}


