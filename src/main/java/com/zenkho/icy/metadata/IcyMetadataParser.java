package com.zenkho.icy.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IcyMetadataParser {

    private static final Logger log = LoggerFactory.getLogger(IcyMetadataParser.class);
    
    private static final Pattern STREAM_TITLE_PATTERN = Pattern.compile("StreamTitle='([^']*)';");
    private static final Pattern STREAM_URL_PATTERN = Pattern.compile("StreamUrl='([^']*)';");

    public String parseStreamTitle(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        Matcher matcher = STREAM_TITLE_PATTERN.matcher(metadata);
        if (matcher.find()) {
            String title = matcher.group(1);
            log.debug("Parsed stream title: {}", title);
            return title;
        }

        return null;
    }

    public String parseStreamUrl(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        Matcher matcher = STREAM_URL_PATTERN.matcher(metadata);
        if (matcher.find()) {
            String url = matcher.group(1);
            log.debug("Parsed stream URL: {}", url);
            return url;
        }

        return null;
    }

    public IcyMetadata parse(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        String title = parseStreamTitle(metadata);
        String url = parseStreamUrl(metadata);

        if (title == null && url == null) {
            return null;
        }

        return new IcyMetadata(title, url);
    }

    public static class IcyMetadata {
        private final String streamTitle;
        private final String streamUrl;

        public IcyMetadata(String streamTitle, String streamUrl) {
            this.streamTitle = streamTitle;
            this.streamUrl = streamUrl;
        }

        public String getStreamTitle() {
            return streamTitle;
        }

        public String getStreamUrl() {
            return streamUrl;
        }

        @Override
        public String toString() {
            return "IcyMetadata{" +
                "streamTitle='" + streamTitle + '\'' +
                ", streamUrl='" + streamUrl + '\'' +
                '}';
        }
    }
}
