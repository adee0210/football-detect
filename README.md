# Football Video Processor

Hệ thống xử lý video bóng đá - Backend API xây dựng với Spring Boot, PostgreSQL, RabbitMQ và Redis.

## Tổng quan

Football Video Processor là một nền tảng cho phép người dùng:

- Tải lên video bóng đá từ máy tính
- Thêm video từ YouTube qua URL
- Xử lý, phân tích video tự động
- Quản lý thư viện video cá nhân

## Công nghệ sử dụng

- **Backend**: Spring Boot 3.4.4, Java 17
- **Database**: PostgreSQL, Flyway (migration)
- **Message Queue**: RabbitMQ
- **Cache**: Redis
- **Object Storage**: MinIO
- **Security**: JWT (JJWT 0.12.3), Spring Security
- **Documentation**: SpringDoc OpenAPI 2.8.6
- **Testing**: JUnit, Testcontainers
- **CI/CD**: Build với Gradle, Docker
- **Công cụ khác**: Lombok, Spring Actuator

## Cấu trúc dự án

```
src/
├── main/
│   ├── java/
│   │   └── com/loopy/footballvideoprocessor/
│   │       ├── common/        # Các component dùng chung
│   │       ├── config/        # Cấu hình ứng dụng
│   │       ├── dashboard/     # Module dashboard
│   │       ├── messaging/     # Xử lý message queue
│   │       ├── security/      # Cấu hình bảo mật
│   │       ├── user/          # Module user
│   │       ├── video/         # Module video
│   │       └── Application.java
│   └── resources/
│       ├── db/migration/      # Flyway migrations
│       ├── application.yml    # Cấu hình chung
│       ├── application-dev.yml # Cấu hình môi trường dev
│       └── application-prod.yml # Cấu hình môi trường prod
└── test/
```

## Các chức năng chính

- **Quản lý người dùng**: Đăng ký, đăng nhập, xác thực email
- **Quản lý video**: Tải lên, thêm từ YouTube, cập nhật thông tin, xóa
- **Xử lý video**: Queue xử lý video tự động, phân tích dữ liệu
- **Dashboard**: Thống kê tổng quan về video và dung lượng

## Cài đặt và chạy

### Yêu cầu

- Java 17+
- Docker và Docker Compose
- Gradle 8.0+

### Thiết lập môi trường development

1. **Clone dự án**

   ```bash
   git clone https://github.com/your-username/football-video-processor.git
   cd football-video-processor
   ```

2. **Cấu hình môi trường**

   ```bash
   cp .env.example .env
   # Chỉnh sửa file .env nếu cần thiết
   ```

3. **Khởi động các dịch vụ với Docker Compose**

   ```bash
   docker-compose up -d
   ```

4. **Biên dịch và chạy ứng dụng**
   ```bash
   ./gradlew bootRun
   ```
5. **Truy cập API và tài liệu**
   - API: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/api/swagger-ui
   - API Docs: http://localhost:8080/api/api-docs
   - Actuator: http://localhost:8080/api/actuator

### Build cho production

```bash
./gradlew clean build
```

## Triển khai

### Sử dụng Docker

Dự án đã có sẵn Dockerfile đa giai đoạn để build và chạy ứng dụng trong môi trường container:

```bash
# Build Docker image
docker build -t football-video-processor .

# Chạy container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  -e DB_PORT=5432 \
  -e DB_NAME=your-db-name \
  -e DB_USERNAME=your-username \
  -e DB_PASSWORD=your-password \
  -e JWT_SECRET=your-jwt-secret \
  football-video-processor
```

Hoặc sử dụng file env:

```bash
docker run -p 8080:8080 --env-file .env.prod football-video-processor
```

### Cấu hình môi trường production

Trong môi trường production, đảm bảo cung cấp các biến môi trường sau:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `VIDEO_UPLOAD_PATH`
- `VIDEO_PROCESSING_SERVICE_URL`
- `SSL_KEY_STORE_PASSWORD`
- Các thông tin kết nối RabbitMQ, Redis, và MinIO

## Bảo mật

- Không commit thông tin nhạy cảm (mật khẩu, khóa API) lên repository
- Sử dụng `.env` cho môi trường development (đã được thêm vào `.gitignore`)
- Trong môi trường production, sử dụng biến môi trường của hệ thống hoặc secret manager
- Dữ liệu nhạy cảm đã được cấu hình trong `.gitignore`:
  - File `.env` và các biến thể của nó
  - Thư mục `logs/` và `uploads/`
  - Các file chứng chỉ SSL trong `src/main/resources/keystore.p12` và `certificates/`

## Phát triển

### Thêm database migration

Dự án sử dụng Flyway để quản lý phiên bản cơ sở dữ liệu:

```bash
# Tạo migration mới
./gradlew createMigration -Pname=your_migration_name
```

### Kiểm thử

Dự án sử dụng JUnit, Spring Test và Testcontainers cho kiểm thử, cùng với JaCoCo cho báo cáo độ phủ:

```bash
# Chạy toàn bộ test
./gradlew test

# Chạy kiểm thử và tạo báo cáo coverage
./gradlew test jacocoTestReport
```

Báo cáo JaCoCo sẽ được tạo trong thư mục `build/reports/jacoco/`.

## Monitoring

Dự án sử dụng Spring Boot Actuator để giám sát và quản lý ứng dụng. Các endpoint được bảo vệ và có thể truy cập tại:

- Health check: http://localhost:8080/api/actuator/health
- Metrics: http://localhost:8080/api/actuator/metrics
- Thông tin: http://localhost:8080/api/actuator/info

## Contribution

1. Fork dự án
2. Tạo branch cho tính năng (`git checkout -b feature/amazing-feature`)
3. Commit thay đổi (`git commit -m 'Add some amazing feature'`)
4. Push lên branch (`git push origin feature/amazing-feature`)
5. Tạo Pull Request

## Giấy phép

Dự án này được phân phối dưới giấy phép MIT. Xem `LICENSE` để biết thêm thông tin.
