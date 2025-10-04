# ⚡ Quick Start - Cài đặt nhanh

## Chỉ cần 2 bước!

### 1️⃣ Thêm vào file `application.yml` của Lavalink:

```yaml
lavalink:
  plugins:
    - dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.0"
      repository: "https://jitpack.io"

plugins:
  icy:
    autoReconnect: true
    enableMetadata: true
```

### 2️⃣ Khởi động lại Lavalink:

```bash
java -jar Lavalink.jar
```

## ✅ Xong! 

Lavalink sẽ tự động tải plugin từ JitPack.

## 🎵 Sử dụng:

```javascript
// Discord.js
player.play("https://streams.ilovemusic.de/iloveradio1.mp3");
```

```python
# Discord.py
track = await wavelink.Playable.search("https://stream.laut.fm/station")
await player.play(track)
```

---

📖 **Chi tiết**: Xem [JITPACK-GUIDE.md](JITPACK-GUIDE.md)
