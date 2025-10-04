# ⚠️ Xử lý sự cố - Troubleshooting

## Vấn đề 1: FileNotFoundException khi tải plugin từ JitPack

### Triệu chứng:
```
Caused by: java.io.FileNotFoundException: 
https://jitpack.io/com/github/ZenKho-chill/zk.icy.stream.lavalink/v1.0.2/zk.icy.stream.lavalink-v1.0.2.jar
```

### Nguyên nhân:
1. **JitPack chưa build xong**: Lần đầu tiên sử dụng một version, JitPack cần thời gian build (30 giây - 2 phút)
2. **Ký tự ẩn trong YAML**: Copy/paste có thể thêm ký tự invisible
3. **Version không tồn tại**: Nhập sai version hoặc tag chưa được tạo

### Giải pháp:

#### Bước 1: Đợi JitPack build xong
Truy cập: https://jitpack.io/#ZenKho-chill/zk.icy.stream.lavalink

Nếu hiển thị "Building..." hoặc không có gì, hãy đợi vài phút rồi thử lại.

#### Bước 2: Verify version tồn tại
Kiểm tra releases: https://github.com/ZenKho-chill/zk.icy.stream.lavalink/releases

Version hiện tại: **v1.0.2** (recommended)

#### Bước 3: Copy cấu hình đúng cách
**KHÔNG copy từ README**, hãy dùng file `application.example.yml` trong repo:

```yaml
lavalink:
  plugins:
    - dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.2"
      repository: "https://jitpack.io"
```

**Lưu ý quan trọng:**
- Phải có dấu ngoặc kép `"` quanh dependency và repository
- Version phải có `v` phía trước (ví dụ: `v1.0.2` chứ KHÔNG phải `1.0.1`)
- Không có khoảng trắng thừa ở cuối dòng

#### Bước 4: Xóa cache và thử lại
Nếu vẫn lỗi, xóa cache plugins của Lavalink:

```bash
# Linux/Mac
rm -rf plugins/*

# Windows
rmdir /s /q plugins
```

Sau đó khởi động lại Lavalink.

---

## Vấn đề 2: ClassNotFoundException

### Triệu chứng:
```
java.lang.ClassNotFoundException: com.zenkho.icy.IcyStreamPlugin
```

### Nguyên nhân:
Bạn đang dùng version cũ (v1.0.0) có lỗi.

### Giải pháp:
Cập nhật lên **v1.0.2**:

```yaml
- dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.2"  # ← Đảm bảo là v1.0.2
```

---

## Vấn đề 2.5: UnsupportedClassVersionError

### Triệu chứng:
```
java.lang.UnsupportedClassVersionError: com/zenkho/icy/IcyStreamPlugin 
has been compiled by a more recent version of the Java Runtime 
(class file version 65.0), this version only recognizes class file versions up to 62.0
```

### Nguyên nhân:
- Version cũ (v1.0.0, v1.0.1) được compile với Java 21
- Lavalink của bạn đang chạy trên Java 18

### Giải pháp:
Cập nhật lên **v1.0.2** (được compile với Java 17):

```yaml
- dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.2"
```

**Hoặc** nâng cấp Java lên 21+:
```bash
# Check Java version
java -version

# Nếu < 21, cài đặt Java 21
# Ubuntu/Debian
sudo apt install openjdk-21-jre

# Docker: Dùng image với Java 21
FROM openjdk:21-slim
```

---

## Vấn đề 3: Plugin không load

### Triệu chứng:
Lavalink khởi động nhưng không thấy log:
```
[INFO] Found plugin 'icy-stream-plugin' version 1.0.1
```

### Giải pháp:

1. **Kiểm tra format YAML đúng không**:
```yaml
lavalink:           # ← Phải căn đều từ đầu dòng
  plugins:          # ← Indent 2 spaces
    - dependency: "com.github.ZenKho-chill:zk.icy.stream.lavalink:v1.0.2"  # ← Indent 4 spaces
      repository: "https://jitpack.io"                                      # ← Indent 6 spaces
```

2. **Kiểm tra Lavalink version**:
Cần Lavalink 4.0.6 trở lên:
```bash
java -jar Lavalink.jar --version
```

3. **Bật debug logging**:
```yaml
logging:
  level:
    lavalink: DEBUG
    com.zenkho.icy: DEBUG
```

---

## Vấn đề 4: Stream không phát được

### Triệu chứng:
Plugin load OK nhưng stream không chơi.

### Giải pháp:

1. **Kiểm tra URL stream có hoạt động không**:
```bash
curl -I https://streams.ilovemusic.de/iloveradio1.mp3
```

Phải trả về status 200 OK hoặc ICY 200 OK.

2. **Bật metadata nếu cần**:
```yaml
plugins:
  icy:
    enableMetadata: true
```

3. **Tăng timeout**:
```yaml
plugins:
  icy:
    connectionTimeout: 20000
    readTimeout: 45000
```

4. **Kiểm tra log để thấy lỗi cụ thể**:
```yaml
logging:
  level:
    com.zenkho.icy: TRACE  # ← Rất chi tiết
```

---

## Vấn đề 5: JitPack 500 Error

### Triệu chứng:
```
Response status code does not indicate success: 500 (Internal Server Error)
```

### Nguyên nhân:
JitPack đang build lần đầu hoặc đang bận.

### Giải pháp:
Đợi 1-2 phút và thử lại. JitPack cần thời gian để build artifact.

Kiểm tra build status:
https://jitpack.io/com/github/ZenKho-chill/zk.icy.stream.lavalink/v1.0.2/build.log

Nếu thấy "BUILD SUCCESSFUL" ở cuối log thì đã OK.

---

## Checklist khi gặp lỗi

- [ ] Đang dùng version v1.0.2 (không phải v1.0.0 hoặc v1.0.2)
- [ ] Java version 17+ (check: `java -version`)
- [ ] Đã đợi JitPack build xong (check https://jitpack.io)
- [ ] YAML format đúng (spaces, không có tabs)
- [ ] Có dấu ngoặc kép `"` quanh dependency và repository
- [ ] Version có chữ `v` phía trước
- [ ] Lavalink version 4.0.6+
- [ ] Không có ký tự ẩn trong file YAML

---

## Vẫn không được?

1. **Download file config mẫu**:
```bash
wget https://raw.githubusercontent.com/ZenKho-chill/zk.icy.stream.lavalink/master/application.example.yml
```

2. **Tạo issue với đầy đủ thông tin**:
   - Lavalink version
   - Java version
   - Full error log
   - File application.yml (ẩn password)

GitHub Issues: https://github.com/ZenKho-chill/zk.icy.stream.lavalink/issues

---

## Test nhanh

Test xem plugin có thể tải được không:

```bash
# Linux/Mac
curl -I https://jitpack.io/com/github/ZenKho-chill/zk.icy.stream.lavalink/v1.0.2/zk.icy.stream.lavalink-v1.0.2.jar

# Windows PowerShell
Invoke-WebRequest -Uri "https://jitpack.io/com/github/ZenKho-chill/zk.icy.stream.lavalink/v1.0.2/zk.icy.stream.lavalink-v1.0.2.jar" -Method Head
```

Nếu return 200 OK hoặc có thể download → Plugin OK, vấn đề ở Lavalink config.
Nếu 404/500 → JitPack chưa build xong, đợi thêm.
