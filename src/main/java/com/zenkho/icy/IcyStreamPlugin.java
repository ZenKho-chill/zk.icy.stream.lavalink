package com.zenkho.icy;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.zenkho.icy.config.IcyConfig;
import com.zenkho.icy.source.IcySourceManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IcyStreamPlugin implements AudioPlayerManagerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IcyStreamPlugin.class);
    private final IcyConfig config;

    public IcyStreamPlugin(IcyConfig config) {
        this.config = config;
        log.info("=================================================");
        log.info("ICY Stream Plugin v1.0.2 initialized");
        log.info("=================================================");
        log.info("Configuration:");
        log.info("  Auto-reconnect: {}", config.isAutoReconnect());
        log.info("  Metadata extraction: {}", config.isEnableMetadata());
        log.info("  Connection timeout: {}ms", config.getConnectionTimeout());
        log.info("  Read timeout: {}ms", config.getReadTimeout());
        log.info("  Max retries: {}", config.getMaxRetries());
        log.info("  Retry delay: {}ms", config.getRetryDelay());
        log.info("  Follow redirects: {}", config.isFollowRedirects());
        log.info("=================================================");
    }

    @Override
    public AudioPlayerManager configure(AudioPlayerManager manager) {
        log.info("Configuring AudioPlayerManager with IcySourceManager...");
        IcySourceManager icySourceManager = new IcySourceManager(config);
        manager.registerSourceManager(icySourceManager);
        log.info("✓ IcySourceManager registered successfully!");
        log.info("ICY Stream Plugin is ready to handle radio streams");
        return manager;
    }
}
