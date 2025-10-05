## 🎯 Major Update: Fix ResponseHeaderFilter Issues

This release addresses the common **ResponseHeaderFilter errors** that occur in Lavalink's Undertow/Spring stack when processing ICY radio streams.

## ✅ Key Improvements

### 🔧 Core Fixes
- Fixed ResponseHeaderFilter errors in Undertow servlet processing
- Enhanced HTTP header sanitization to handle special characters
- Improved metadata parsing with robust error recovery
- Better connection management with graceful error handling

### 🚀 Performance Enhancements  
- Increased default timeouts for better stability
- Optimized buffer sizes for ICY stream processing
- Better resource cleanup to prevent memory leaks
- Enhanced reconnection logic for unstable streams

### 📝 Configuration Improvements
- Updated application.example.yml with optimized Undertow settings
- Enhanced troubleshooting guide with specific solutions
- Better logging configuration for debugging

## 🔧 How to Use

Add to your Lavalink `application.yml`:

```yaml
lavalink:
  plugins:
    - dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.1.0"
      repository: "https://jitpack.io"
```

## 🎵 Supported Streams

- ✅ ICY/SHOUTcast radio streams
- ✅ HTTP(S) audio streams  
- ✅ Metadata extraction (song titles, artist info)
- ✅ Auto-reconnection for unstable streams
- ✅ Multiple audio formats (MP3, AAC, etc.)

## 📋 Requirements

- **Lavalink**: 4.0.6 or higher
- **Java**: 17 or higher  
- **Spring Boot**: 3.x compatible
- **Undertow**: 2.3.x compatible

## 🆘 Troubleshooting

If you encounter issues, check:
- `TROUBLESHOOTING.md` for common problems
- `FIX-RESPONSEHEADERFILTER.md` for specific ResponseHeaderFilter solutions

## 📦 JitPack Status

This release is optimized for JitPack build system. 
Build status: https://jitpack.io/#ZenKho-chill/zk.icy.stream.lavalink/v1.1.0