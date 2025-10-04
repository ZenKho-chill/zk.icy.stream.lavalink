# FILE BUILD - HƯỚNG DẪN SỬ DỤNG

## 📦 Thông tin File

**Tên file**: zk-icy-stream-lavalink-1.0.0.jar
**Vị trí**: D:\zk icy\build\libs\zk-icy-stream-lavalink-1.0.0.jar
**Kích thước**: 2.81 MB
**Tác giả**: ZenKho
**Package**: com.zenkho.icy

## 🚀 Cách Deploy

### Phương pháp 1: Manual (Copy JAR file)

1. Copy file JAR vào thư mục plugins của Lavalink:
   ```
   D:\zk icy\build\libs\zk-icy-stream-lavalink-1.0.0.jar
   → 
   /path/to/lavalink/plugins/
   ```

2. Restart Lavalink server

3. Kiểm tra log xem plugin đã load:
   ```
   [INFO] ICY Stream Plugin initialized
   [INFO] Registered IcySourceManager with Lavalink
   ```

### Phương pháp 2: JitPack (Tự động tải)

1. Thêm vào file application.yml của Lavalink:
   ```yaml
   lavalink:
     plugins:
       - dependency: com.github.zenkho:lavalink-icy-stream-plugin:1.0.0
         repository: https://jitpack.io
   ```

2. Restart Lavalink - plugin sẽ tự động được tải

## ⚙️ Cấu hình Plugin

Thêm vào application.yml:

```yaml
plugins:
  icy:
    autoReconnect: true        # Tự động kết nối lại khi mất kết nối
    enableMetadata: true       # Trích xuất metadata (tên bài hát)
    connectionTimeout: 10000   # Timeout kết nối (ms)
    readTimeout: 30000         # Timeout đọc dữ liệu (ms)
    maxRetries: 3              # Số lần thử lại tối đa
    retryDelay: 2000           # Delay giữa các lần thử (ms)
    followRedirects: true      # Tự động follow redirects
```

## 📝 Test Plugin

Sau khi cài đặt, test bằng cách play radio stream:

```
/play https://streams.ilovemusic.de/iloveradio1.mp3
/play https://stream.laut.fm/yourstation
```

Plugin sẽ tự động nhận diện và xử lý ICY streams!

## 🔄 Build Lại (nếu cần)

Nếu bạn thay đổi code và cần build lại:

```bash
cd "D:\zk icy"
./gradlew clean build
```

File mới sẽ được tạo tại: `build\libs\zk-icy-stream-lavalink-1.0.0.jar`

## 📚 Documentation

Xem file README.md để biết thêm chi tiết về:
- Cấu hình nâng cao
- Troubleshooting
- Performance tuning
- Security
- Monitoring

## ✅ Checklist Deploy

- [ ] Copy JAR vào plugins/ hoặc cấu hình JitPack
- [ ] Thêm config vào application.yml (optional)
- [ ] Restart Lavalink
- [ ] Check logs xem plugin đã load
- [ ] Test với 1 radio stream URL
- [ ] Verify metadata extraction hoạt động

## 🆘 Troubleshooting

**Plugin không load?**
- Check Java version (phải 21+): `java -version`
- Verify file JAR trong thư mục plugins/
- Check Lavalink logs

**Stream không phát?**
- Test URL: `curl -I <stream_url>`
- Enable debug logging: `com.zenkho.icy: DEBUG`
- Check network connectivity

**Metadata không hiện?**
- Verify `enableMetadata: true`
- Không phải stream nào cũng có metadata
- Check logs cho metadata parsing

## 📞 Support

- README.md - Hướng dẫn chi tiết
- CHANGELOG.md - Lịch sử thay đổi
- GitHub Issues - Báo lỗi và yêu cầu tính năng
