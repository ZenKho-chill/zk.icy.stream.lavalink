# Changelog

All notable changes to the ICY Stream Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-01-04

### Added
- Initial release of ICY Stream Plugin for Lavalink v4
- Support for ICY protocol (Icecast/SHOUTcast)
- HTTP/HTTPS stream support
- Automatic redirect handling (301, 302, 307, 308)
- Dynamic URL support with query parameters
- Chunked transfer encoding support
- Auto-reconnection on connection loss
- ICY metadata extraction (StreamTitle, StreamUrl)
- Configurable timeouts and retry logic
- Spring Boot configuration support
- Support for MP3, AAC, and AACP streams
- Support for HLS and M3U8 playlists
- Comprehensive error handling
- Detailed logging

### Supported Formats
- Audio: MP3, AAC, AACP
- Playlists: M3U8, HLS, PLS
- Protocols: HTTP, HTTPS, ICY

### Configuration Options
- `autoReconnect`: Enable/disable automatic reconnection
- `enableMetadata`: Enable/disable metadata extraction
- `connectionTimeout`: Connection timeout in milliseconds
- `readTimeout`: Read timeout in milliseconds
- `maxRetries`: Maximum reconnection attempts
- `retryDelay`: Delay between retry attempts
- `followRedirects`: Enable/disable HTTP redirect following

### Technical Details
- Built for Java 21
- Compatible with Lavalink 4.0.6+
- Uses OkHttp for HTTP client
- Implements AudioSourceManager interface
- Custom SeekableInputStream for non-seekable streams

### Known Limitations
- Metadata update events not yet emitted to Lavalink client (logged only)
- No seeking support (by design, streams are live)
- No playlist parsing (M3U/PLS files - planned for future)
- No adaptive bitrate (planned for future)

## [Unreleased]

### Planned Features
- [ ] Emit metadata update events to Lavalink client
- [ ] M3U/PLS playlist parsing and segment handling
- [ ] Adaptive bitrate switching
- [ ] Multiple fallback URL support
- [ ] Stream quality detection
- [ ] Bandwidth monitoring and metrics
- [ ] Custom User-Agent per stream
- [ ] Stream recording capability
- [ ] Health check endpoints
- [ ] Prometheus metrics export

### Planned Improvements
- [ ] Better error messages
- [ ] Performance optimizations
- [ ] Memory usage optimizations
- [ ] Unit tests
- [ ] Integration tests
- [ ] Documentation improvements

## Version History

### Version Compatibility

| Plugin Version | Lavalink Version | Java Version |
|---------------|------------------|--------------|
| 1.0.0         | 4.0.6+           | 21+          |

### Migration Guides

#### From no ICY support to 1.0.0

No migration needed. Simply install the plugin and existing radio stream URLs will start working.

**Before**:
```
Error: Could not load stream
```

**After**:
```
Successfully loaded: Radio Station - Genre (128kbps)
```

### Breaking Changes

None (initial release)

## Development Notes

### Build Information
- Build Tool: Gradle 8.2
- Kotlin DSL: Yes
- Plugin System: Lavalink Gradle Plugin 1.0.15

### Dependencies
- Lavalink API: 4.0.6
- OkHttp: 4.12.0
- SLF4J: 2.0.9
- Spring Boot: (inherited from Lavalink)

### Repository Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/zenkho/icy/
│   │       ├── IcyStreamPlugin.java
│   │       ├── config/
│   │       │   └── IcyConfig.java
│   │       ├── source/
│   │       │   └── IcySourceManager.java
│   │       ├── stream/
│   │       │   ├── IcyHttpStream.java
│   │       │   └── IcyStreamAudioTrack.java
│   │       └── metadata/
│   │           └── IcyMetadataParser.java
│   └── resources/
│       └── application.yml
```

## Contributing

See [coding-standards.md](laws/coding-standards.md) for coding guidelines (internal use only).

## License

MIT License

## Acknowledgments

- Lavalink team for the excellent framework
- Icecast/SHOUTcast for the ICY protocol
- All radio stations for providing streams
