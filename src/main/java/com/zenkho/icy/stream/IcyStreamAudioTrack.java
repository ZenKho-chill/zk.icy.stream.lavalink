package com.zenkho.icy.stream;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import com.zenkho.icy.IcyStreamPlugin.Config;
import com.zenkho.icy.source.IcySourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcyStreamAudioTrack extends DelegatedAudioTrack {

    private static final Logger log = LoggerFactory.getLogger(IcyStreamAudioTrack.class);

    private final IcySourceManager sourceManager;
    private final Config config;

    public IcyStreamAudioTrack(AudioTrackInfo trackInfo, IcySourceManager sourceManager, Config config) {
        super(trackInfo);
        this.sourceManager = sourceManager;
        this.config = config;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        try (IcyHttpStream httpStream = new IcyHttpStream(
            trackInfo.identifier,
            sourceManager.getHttpClient(),
            config
        )) {
            log.info("Processing stream: {}", trackInfo.title);
            
            // Get the input stream
            SeekableInputStream inputStream = httpStream.getInputStream();
            
            // Use the source manager's internal track processing
            // LavaPlayer will automatically detect the format and process it
            processDelegate(this, executor);
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new IcyStreamAudioTrack(trackInfo, sourceManager, config);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }
}

