# Hướng dẫn sử dụng Plugin từ JitPack

## 📦 Cách cài đặt Plugin từ JitPack

Plugin này có thể được tự động tải xuống và cài đặt bởi Lavalink từ JitPack chỉ với vài dòng cấu hình.

### Bước 1: Cấu hình Lavalink

Mở file `application.yml` của Lavalink và thêm cấu hình sau:

```yaml
lavalink:
  plugins:
    - dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.3"
      repository: "https://jitpack.io"
```

### Bước 2: Cấu hình Plugin (Tùy chọn)

Thêm cấu hình cho plugin ICY Stream nếu muốn tùy chỉnh:

```yaml
plugins:
  icy:
    # Tự động kết nối lại khi mất kết nối
    autoReconnect: true
    
    # Bật trích xuất metadata (tên bài hát đang phát)
    enableMetadata: true
    
    # Timeout kết nối (milliseconds)
    connectionTimeout: 10000
    
    # Timeout đọc dữ liệu (milliseconds)
    readTimeout: 30000
    
    # Số lần thử kết nối lại tối đa
    maxRetries: 3
    
    # Độ trễ giữa các lần thử kết nối lại (milliseconds)
    retryDelay: 2000
    
    # Tự động theo dõi HTTP redirects
    followRedirects: true
```

### Bước 3: Khởi động lại Lavalink

```bash
java -jar Lavalink.jar
```

Lavalink sẽ tự động:
1. Tải plugin từ JitPack
2. Cài đặt vào thư mục plugins
3. Kích hoạt plugin

## 🔄 Cập nhật phiên bản mới

Khi có phiên bản mới, chỉ cần thay đổi số phiên bản trong `application.yml`:

```yaml
lavalink:
  plugins:
    - dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.3"
      repository: "https://jitpack.io"
```

Sau đó khởi động lại Lavalink.

## 📋 File application.yml hoàn chỉnh

Ví dụ file `application.yml` đầy đủ:

```yaml
server:
  port: 2333
  address: 0.0.0.0

lavalink:
  plugins:
    - dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.3"
      repository: "https://jitpack.io"
  
  server:
    password: "youshallnotpass"
    sources:
      youtube: true
      bandcamp: true
      soundcloud: true
      twitch: true
      vimeo: true
      http: true
      local: false
    bufferDurationMs: 400
    frameBufferDurationMs: 5000
    youtubePlaylistLoadLimit: 6
    playerUpdateInterval: 5
    youtubeSearchEnabled: true
    soundcloudSearchEnabled: true
    gc-warnings: true

plugins:
  icy:
    autoReconnect: true
    enableMetadata: true
    connectionTimeout: 10000
    readTimeout: 30000
    maxRetries: 3
    retryDelay: 2000
    followRedirects: true

logging:
  level:
    root: INFO
    lavalink: INFO
    com.zenkho.icy: DEBUG
```

## 🎯 Sử dụng Plugin

Sau khi cài đặt, bạn có thể phát các radio stream trực tiếp:

### Ví dụ Discord Bot (Discord.js)

```javascript
// Phát radio stream
player.play("https://streams.ilovemusic.de/iloveradio1.mp3");
```

### Ví dụ Discord Bot (Discord.py)

```python
# Phát radio stream
track = await wavelink.Playable.search("https://stream.laut.fm/station")
await player.play(track)
```

## 🔍 Kiểm tra Plugin đã hoạt động

Khi Lavalink khởi động, bạn sẽ thấy log:

```
[INFO] ICY Stream Plugin initialized
[INFO] ICY Stream Plugin v1.0.0 loaded successfully
```

## ⚠️ Lưu ý quan trọng

1. **Phiên bản JitPack**: Luôn thêm `v` trước số phiên bản (ví dụ: `v1.0.0`, không phải `1.0.0`)
2. **Repository URL**: Phải là `https://jitpack.io` (có HTTPS)
3. **Dependency Format**: `com.github.USERNAME:REPO-NAME:VERSION`
4. **Internet**: Lavalink cần kết nối internet để tải plugin lần đầu

## 🚀 Các phiên bản có sẵn

Xem tất cả phiên bản tại: https://jitpack.io/#ZenKho-chill/zk.icy.stream.lavalink

- `v1.0.0` - Phiên bản đầu tiên (stable)
- Các phiên bản tiếp theo sẽ được cập nhật...

## 🐛 Xử lý sự cố

### Plugin không tải được

**Kiểm tra**:
```yaml
# Đảm bảo format đúng với dấu ngoặc kép
- dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.0"
  repository: "https://jitpack.io"
```

**Xem log chi tiết**:
```yaml
logging:
  level:
    lavalink: DEBUG
```

### JitPack build failed

1. Truy cập https://jitpack.io/#ZenKho-chill/zk.icy.stream.lavalink
2. Click vào phiên bản để xem build log
3. Nếu build fail, báo lỗi tại Issues

## 💡 Tips

- **Cache**: JitPack cache plugin sau lần tải đầu tiên
- **Offline Mode**: Sau khi tải, plugin hoạt động offline
- **Multiple Nodes**: Mỗi Lavalink node cần cấu hình riêng
- **Docker**: Cấu hình tương tự, mount file `application.yml`

---

**Chúc bạn sử dụng plugin thành công! 🎵**
