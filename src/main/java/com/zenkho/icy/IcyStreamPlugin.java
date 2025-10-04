package com.zenkho.icy;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.zenkho.icy.config.IcyConfig;
import com.zenkho.icy.source.IcySourceManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IcyStreamPlugin implements AudioPlayerManagerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IcyStreamPlugin.class);
    private final IcyConfig config;

    public IcyStreamPlugin(IcyConfig config) {
        this.config = config;
        log.info("ICY Stream Plugin initialized");
        log.info("Auto-reconnect: {}", config.isAutoReconnect());
        log.info("Metadata extraction: {}", config.isEnableMetadata());
    }

    @Override
    public AudioPlayerManager configure(AudioPlayerManager manager) {
        IcySourceManager icySourceManager = new IcySourceManager(config);
        manager.registerSourceManager(icySourceManager);
        log.info("Registered IcySourceManager with Lavalink");
        return manager;
    }
}
