package com.zenkho.icy.stream;

import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.zenkho.icy.config.IcyConfig;
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
    private final IcyConfig config;
    private Response response;
    private IcyStreamInputStream inputStream;
    private int icyMetaInt;
    private String streamTitle;
    private int reconnectAttempts;

    public IcyHttpStream(String url, OkHttpClient httpClient, IcyConfig config) throws IOException {
        this.url = url;
        this.httpClient = httpClient;
        this.config = config;
        this.reconnectAttempts = 0;
        connect();
    }

    private void connect() throws IOException {
        Request request = new Request.Builder()
            .url(url)
            .header("Icy-MetaData", "1")
            .header("User-Agent", "Lavalink ICY Stream Plugin/1.0")
            .build();

        response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            response.close();
            throw new IOException("Unexpected response code: " + response.code());
        }

        // Parse ICY headers
        String icyMetaIntStr = response.header("icy-metaint");
        if (icyMetaIntStr != null) {
            try {
                icyMetaInt = Integer.parseInt(icyMetaIntStr);
                log.info("ICY metadata interval: {}", icyMetaInt);
            } catch (NumberFormatException e) {
                log.warn("Invalid icy-metaint header: {}", icyMetaIntStr);
                icyMetaInt = 0;
            }
        } else {
            icyMetaInt = 0;
        }

        String icyName = response.header("icy-name");
        String icyGenre = response.header("icy-genre");
        String icyBr = response.header("icy-br");

        log.info("Connected to stream - Name: {}, Genre: {}, Bitrate: {}kbps", 
            icyName, icyGenre, icyBr);

        ResponseBody body = response.body();
        if (body == null) {
            response.close();
            throw new IOException("Response body is null");
        }

        InputStream rawStream = body.byteStream();
        
        if (icyMetaInt > 0 && config.isEnableMetadata()) {
            inputStream = new IcyMetadataInputStream(rawStream, icyMetaInt);
        } else {
            inputStream = new IcyStreamInputStream(rawStream);
        }
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
            int size = delegate.read();
            if (size < 0) {
                throw new IOException("Unexpected end of stream while reading metadata size");
            }

            int metadataLength = size * 16;
            if (metadataLength == 0) {
                return;
            }

            byte[] metadataBytes = new byte[metadataLength];
            int bytesRead = 0;
            while (bytesRead < metadataLength) {
                int result = delegate.read(metadataBytes, bytesRead, metadataLength - bytesRead);
                if (result < 0) {
                    throw new IOException("Unexpected end of stream while reading metadata");
                }
                bytesRead += result;
            }

            String metadata = new String(metadataBytes, "UTF-8").trim();
            String title = metadataParser.parseStreamTitle(metadata);
            
            if (title != null && !title.equals(streamTitle)) {
                streamTitle = title;
                log.info("Now playing: {}", streamTitle);
                // TODO: Emit metadata update event to Lavalink
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
