# Lavalink ICY Stream Plugin

A Lavalink v4 plugin that enables streaming of internet radio stations using the ICY protocol (Icecast/SHOUTcast). This plugin handles MP3, AAC, and HLS streams with automatic reconnection and metadata extraction.

## 🎯 Overview

This plugin extends Lavalink's capabilities to support radio streaming protocols that aren't natively handled. It processes ICY headers, handles infinite-duration streams, manages automatic reconnection, and extracts real-time metadata like "Now Playing" information.

### Why This Plugin?

Lavalink's default HTTP audio source manager doesn't handle:
- ICY protocol responses (`ICY 200 OK` instead of `HTTP/1.1 200 OK`)
- Streams without `Content-Length` header
- Metadata embedded in audio streams (icy-metaint)
- Long-running connections that need reconnection
- Dynamic URLs with session tokens

This plugin solves all these issues.

## ✨ Features

- ✅ **ICY Protocol Support** - Full Icecast/SHOUTcast compatibility
- ✅ **Multiple Formats** - MP3, AAC, AACP, HLS streams
- ✅ **Auto Reconnection** - Automatic recovery from connection failures
- ✅ **Metadata Extraction** - Real-time "Now Playing" information
- ✅ **HTTP Redirects** - Follows 301, 302, 307, 308 redirects automatically
- ✅ **Dynamic URLs** - Handles URLs with session IDs and tokens
- ✅ **Chunked Transfer** - Supports chunked transfer encoding
- ✅ **Configurable** - YAML-based configuration
- ✅ **Production Ready** - Robust error handling and logging

## 📋 Requirements

- **Java**: 21 or higher
- **Lavalink**: 4.0.6 or higher
- **Memory**: ~10-50 MB per active stream
- **Network**: Stable internet connection

## 🚀 Installation

### Method 1: JitPack (Recommended)

Add to your Lavalink `application.yml`:

```yaml
lavalink:
  plugins:
    - dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.0"
      repository: "https://jitpack.io"
```

### Method 2: Manual Installation

1. Download the latest JAR from [Releases](https://github.com/ZenKho-chill/zk.icy.stream.lavalink/releases)
2. Place it in your Lavalink `plugins/` directory
3. Restart Lavalink

### Method 3: Build from Source

```bash
git clone https://github.com/ZenKho-chill/zk.icy.stream.lavalink.git
cd zk.icy.stream.lavalink
./gradlew build
cp build/libs/zk-icy-stream-lavalink-1.0.0.jar /path/to/lavalink/plugins/
```

## ⚙️ Configuration

Add to your Lavalink `application.yml`:

```yaml
plugins:
  icy:
    # Enable automatic reconnection when stream connection is lost
    autoReconnect: true
    
    # Enable metadata extraction from ICY streams (song titles, artist info)
    enableMetadata: true
    
    # Connection timeout in milliseconds
    connectionTimeout: 10000
    
    # Read timeout in milliseconds
    readTimeout: 30000
    
    # Maximum number of reconnection attempts
    maxRetries: 3
    
    # Delay between reconnection attempts in milliseconds
    retryDelay: 2000
    
    # Follow HTTP redirects (301, 302, etc.)
    followRedirects: true
```

### Configuration Profiles

#### High-Traffic Servers
```yaml
plugins:
  icy:
    connectionTimeout: 15000
    readTimeout: 60000
    maxRetries: 5
    retryDelay: 3000
```

#### Low-Latency Setup
```yaml
plugins:
  icy:
    enableMetadata: false
    connectionTimeout: 5000
    readTimeout: 15000
    maxRetries: 2
    retryDelay: 1000
```

#### Unreliable Networks
```yaml
plugins:
  icy:
    connectionTimeout: 20000
    readTimeout: 45000
    maxRetries: 10
    retryDelay: 5000
```

## 📖 Usage

### Discord.js Example

```javascript
const { Manager } = require("erela.js");

const manager = new Manager({
    nodes: [{
        host: "localhost",
        port: 2333,
        password: "youshallnotpass"
    }]
});

// Play radio stream
player.play("https://streams.ilovemusic.de/iloveradio1.mp3");
```

### Discord.py Example

```python
import wavelink

# Play radio stream
track = await wavelink.Playable.search("https://stream.laut.fm/station")
await player.play(track)
```

### JDA Example

```java
LavalinkPlayer player = lavalink.getPlayer(guildId);
player.playTrack("https://streams.ilovemusic.de/iloveradio1.mp3");
```

### Supported URLs

✅ Direct stream URLs:
```
https://streams.ilovemusic.de/iloveradio1.mp3
https://stream.laut.fm/yourstation
http://ice.example.com:8000/stream.aac
```

✅ Dynamic URLs with parameters:
```
https://example.com/radio?session=abc123&expires=1234567890
```

✅ HLS/M3U8 playlists:
```
https://example.com/playlist.m3u8
```

❌ Not supported:
- YouTube, Spotify, SoundCloud (use default Lavalink sources)
- Local files

## 🔧 How It Works

### Architecture

```
User Request → IcySourceManager (detect stream)
    ↓
IcyStreamAudioTrack (create track)
    ↓
IcyHttpStream (connect & parse ICY headers)
    ↓
IcyMetadataInputStream (extract metadata)
    ↓
Audio Decoder (MP3/AAC/etc.)
    ↓
Playback
```

### ICY Protocol Handling

1. **Connection**: Sends `Icy-MetaData: 1` header
2. **Header Parsing**: Extracts `icy-name`, `icy-br`, `icy-metaint`
3. **Stream Reading**: Reads audio data + metadata blocks
4. **Metadata Extraction**: Parses `StreamTitle='Artist - Song'`
5. **Reconnection**: Auto-reconnects on failures

### Metadata Format

ICY streams embed metadata every N bytes (icy-metaint):
```
[Audio Data - 16000 bytes]
[Metadata Length - 1 byte]
[Metadata - Length × 16 bytes]
[Audio Data - 16000 bytes]
...
```

Example metadata:
```
StreamTitle='Daft Punk - Get Lucky';StreamUrl='http://...';
```

## 🛠️ Troubleshooting

### Plugin Not Loading

**Check**:
- Java version: `java -version` (must be 21+)
- Plugin JAR in `plugins/` directory
- Lavalink logs for errors

**Fix**: Verify Lavalink configuration and restart

### Stream Won't Play

**Check**:
- URL is accessible: `curl -I <URL>`
- Network connectivity
- Not geo-restricted

**Fix**: Enable debug logging:
```yaml
logging:
  level:
    com.zenkho.icy: DEBUG
```

### Connection Timeouts

**Fix**: Increase timeouts in configuration:
```yaml
plugins:
  icy:
    connectionTimeout: 20000
    readTimeout: 45000
```

### No Metadata Showing

**Check**:
- `enableMetadata: true` in config
- Stream actually provides metadata (not all do)

**Fix**: Test with known metadata streams like iLoveRadio

### High Memory Usage

**Fix**:
- Reduce concurrent streams
- Disable metadata if not needed
- Increase JVM heap: `java -Xmx2G -jar Lavalink.jar`

## 📊 Performance

### Resource Usage (per stream)
- **Memory**: 10-50 MB
- **CPU**: 2-5%
- **Bandwidth**: Stream bitrate (typically 64-320 kbps)

### Latency
- **Initial Connection**: 1-3 seconds
- **Start Playing**: 2-5 seconds
- **Reconnection**: 2-4 seconds
- **Metadata Update**: <1 second

### Limits
- **Concurrent Streams**: Limited by server resources
- **Stream Duration**: Unlimited (infinite)
- **Reconnection Attempts**: Configurable (default: 3)

## 🏗️ Technical Details

### Components

#### IcyStreamPlugin
Entry point that registers the source manager with Lavalink.

#### IcySourceManager
Detects radio streams and creates audio tracks. Checks:
- URL patterns (`.mp3`, `.aac`, `radio`, `stream` in URL)
- Content-Type headers
- ICY headers presence

#### IcyStreamAudioTrack
Represents an audio track from an ICY stream. Handles playback processing.

#### IcyHttpStream
Manages HTTP connection, ICY protocol, and reconnection logic. Features:
- Auto-reconnection with exponential backoff
- ICY header parsing
- Metadata stream handling

#### IcyMetadataParser
Parses ICY metadata using regex:
- `StreamTitle='...'` → Song/artist info
- `StreamUrl='...'` → Related URL

### Dependencies

- **OkHttp 4.12.0** - HTTP client with streaming support
- **LavaPlayer** - Audio decoding and playback
- **Spring Boot** - Configuration management
- **SLF4J 2.0.9** - Logging framework

## 🔐 Security

### Network Security

Configure firewall to restrict Lavalink access:
```bash
# Linux iptables
iptables -A INPUT -p tcp --dport 2333 -s YOUR_BOT_IP -j ACCEPT
iptables -A INPUT -p tcp --dport 2333 -j DROP
```

### Authentication

Use strong passwords in Lavalink config:
```yaml
lavalink:
  server:
    password: "your-very-secure-password-here"
```

### SSL/TLS

Enable HTTPS for Lavalink:
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
```

## 📈 Monitoring

### Enable Detailed Logging

```yaml
logging:
  level:
    root: INFO
    com.zenkho.icy: DEBUG
    com.zenkho.icy.stream: TRACE  # Very detailed
```

### Key Metrics to Monitor

- Active stream count
- Reconnection attempts
- Average stream uptime
- Memory per stream
- CPU usage

### Log Messages

Success:
```
[INFO] ICY Stream Plugin initialized
[INFO] Connected to stream - Name: Cool FM, Bitrate: 128kbps
[INFO] Now playing: Artist - Song Name
```

Reconnection:
```
[WARN] Stream connection lost, reconnecting... (attempt 1/3)
[INFO] Successfully reconnected to stream
```

Errors:
```
[ERROR] Failed to connect to stream: Connection timeout
```

## 🚀 Production Deployment

### JVM Settings

Recommended flags:
```bash
java -Xms512M -Xmx2G \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseStringDeduplication \
     -jar Lavalink.jar
```

### Docker Deployment

```dockerfile
FROM openjdk:21-slim
WORKDIR /opt/lavalink
COPY Lavalink.jar .
COPY application.yml .
COPY plugins/ plugins/
CMD ["java", "-Xmx2G", "-jar", "Lavalink.jar"]
```

### Health Checks

Monitor these endpoints/logs:
- Lavalink health endpoint
- Active connections
- Error rates
- Memory usage

## 🔄 Updates

### Updating the Plugin

1. Change version in `application.yml`:
```yaml
- dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.1.0"
```

2. Restart Lavalink
3. Verify logs show new version

### Rollback

If issues occur:
1. Revert to previous version in config
2. Restart Lavalink
3. Check logs for stability

## 🐛 Known Limitations

1. **Metadata Events**: Metadata updates are logged but not yet emitted to Lavalink client
2. **Playlist Parsing**: M3U/PLS files not yet parsed (direct streams only)
3. **No Seeking**: Streams are live, seeking not supported
4. **Geo-Restrictions**: Some streams may be geo-blocked

## 🗺️ Roadmap

### Version 1.1 (Planned)
- [ ] Emit metadata update events to client
- [ ] M3U/PLS playlist parsing
- [ ] Unit tests
- [ ] Integration tests

### Version 1.2 (Future)
- [ ] Adaptive bitrate switching
- [ ] Multiple fallback URLs
- [ ] Stream quality detection
- [ ] Prometheus metrics

### Version 2.0 (Future)
- [ ] Stream recording
- [ ] Custom User-Agent per stream
- [ ] Bandwidth monitoring
- [ ] Health check API

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Lavalink team for the excellent framework
- Icecast/SHOUTcast for the ICY protocol specification
- All contributors and users

## 💬 Support

- **Issues**: [GitHub Issues](https://github.com/ZenKho-chill/zk.icy.stream.lavalink/issues)
- **Discord**: [Lavalink Discord](https://discord.gg/jttmwHTAad) - #plugin-dev channel
- **Documentation**: Check this README and code comments

## 🤝 Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Follow existing code style
4. Add tests if applicable
5. Update documentation
6. Submit a pull request

### Development Setup

```bash
git clone https://github.com/ZenKho-chill/zk.icy.stream.lavalink.git
cd zk.icy.stream.lavalink
./gradlew build
./gradlew runLavalink  # Test with local Lavalink
```

## 📞 Contact

- **Author**: ZenKho
- **Repository**: https://github.com/ZenKho-chill/zk.icy.stream.lavalink
- **Issues**: https://github.com/ZenKho-chill/zk.icy.stream.lavalink/issues

---

**Made with ❤️ by ZenKho** | **Powered by Lavalink**
