# Fix cho lỗi ResponseHeaderFilter - ICY Stream Plugin v1.1.0

## Tóm tắt các cải tiến

Phiên bản 1.1.0 đã được cập nhật để khắc phục lỗi `ResponseHeaderFilter` trong stack trace Undertow/Spring của bạn.

### Các thay đổi chính:

1. **Cải thiện HTTP header handling**
   - Thêm header sanitization để loại bỏ ký tự đặc biệt
   - Xử lý encoding issues
   - Thêm các headers cần thiết (Accept, Connection)

2. **Better error handling**
   - Xử lý IOException một cách graceful
   - Logging chi tiết hơn cho debugging
   - Tránh crash khi gặp lỗi metadata

3. **Cải thiện timeout và retry logic**
   - Tăng default timeout values
   - Better connection management
   - Improved reconnection handling

4. **Stream processing enhancements**
   - Safer metadata parsing
   - Better resource cleanup
   - Improved error recovery

## Cách sử dụng:

### 1. Cập nhật dependency trong application.yml

```yaml
lavalink:
  plugins:
    - dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.1.0"
      repository: "https://jitpack.io"
```

### 2. Sử dụng cấu hình được đề xuất

Copy file `application.example.yml` trong repo này, nó đã bao gồm:
- Undertow configuration tối ưu
- Timeout values tăng cường
- Buffer sizes phù hợp
- Debug logging setup

### 3. Nếu vẫn gặp lỗi

1. **Tắt metadata parsing tạm thời:**
```yaml
plugins:
  icy:
    enableMetadata: false
```

2. **Tăng timeout:**
```yaml
plugins:
  icy:
    connectionTimeout: 30000
    readTimeout: 60000
```

3. **Bật debug logging:**
```yaml
logging:
  level:
    com.zenkho.icy: DEBUG
    lavalink.server.io: DEBUG
```

## Test plugin:

```bash
# Test plugin có tải được không
curl -I https://jitpack.io/com/github/ZenKho-chill/zk.icy.stream.lavalink/v1.1.0/zk.icy.stream.lavalink-v1.1.0.jar

# Test stream URL cụ thể
curl -I -H "Icy-MetaData: 1" "https://your-stream-url.mp3"
```

## Changelog v1.1.0:

- ✅ Fixed ResponseHeaderFilter issues
- ✅ Improved HTTP header sanitization
- ✅ Better error handling for stream connections
- ✅ Enhanced metadata parsing with error recovery
- ✅ Increased default timeout values
- ✅ Better resource cleanup
- ✅ Improved logging for debugging

Nếu vẫn gặp vấn đề, vui lòng tạo issue trên GitHub với full error log và cấu hình của bạn.