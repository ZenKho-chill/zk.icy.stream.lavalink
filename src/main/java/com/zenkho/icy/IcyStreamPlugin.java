package com.zenkho.icy;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.zenkho.icy.source.IcySourceManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IcyStreamPlugin implements AudioPlayerManagerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IcyStreamPlugin.class);

    // NO-ARGS CONSTRUCTOR - Đây là key!
    public IcyStreamPlugin() {
        log.info("=================================================");
        log.info("ICY Stream Plugin v1.1.1 loaded");
        log.info("=================================================");
    }

    @Override
    public AudioPlayerManager configure(AudioPlayerManager manager) {
        log.info("Configuring AudioPlayerManager with IcySourceManager...");
        
        // Create config with hardcoded defaults
        Config config = new Config();
        
        log.info("Configuration:");
        log.info("  Auto-reconnect: {}", config.autoReconnect);
        log.info("  Metadata extraction: {}", config.enableMetadata);
        log.info("  Connection timeout: {}ms", config.connectionTimeout);
        log.info("  Read timeout: {}ms", config.readTimeout);
        log.info("  Max retries: {}", config.maxRetries);
        log.info("  Retry delay: {}ms", config.retryDelay);
        log.info("  Follow redirects: {}", config.followRedirects);
        
        IcySourceManager icySourceManager = new IcySourceManager(config);
        manager.registerSourceManager(icySourceManager);
        
        log.info("=================================================");
        log.info("✓ IcySourceManager registered successfully!");
        log.info("ICY Stream Plugin is ready to handle radio streams");
        log.info("=================================================");
        
        return manager;
    }
    
    // Simple config class
    public static class Config {
        public boolean autoReconnect = true;
        public boolean enableMetadata = true;
        public int connectionTimeout = 10000;
        public int readTimeout = 30000;
        public int maxRetries = 3;
        public int retryDelay = 2000;
        public boolean followRedirects = true;

        public boolean isAutoReconnect() { return autoReconnect; }
        public boolean isEnableMetadata() { return enableMetadata; }
        public int getConnectionTimeout() { return connectionTimeout; }
        public int getReadTimeout() { return readTimeout; }
        public int getMaxRetries() { return maxRetries; }
        public int getRetryDelay() { return retryDelay; }
        public boolean isFollowRedirects() { return followRedirects; }
    }
}
