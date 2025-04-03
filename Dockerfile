FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Sao chép các tệp build
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./
COPY src ./src

# Build ứng dụng, bỏ qua các bài kiểm tra
RUN gradle build --no-daemon -x test

# Lấy artifact vừa build
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cài đặt các công cụ và dependencies cần thiết
RUN apk add --no-cache curl

# Sao chép JAR từ stage build
COPY --from=build /app/build/libs/*.jar app.jar

# Tạo thư mục cho tệp tải lên
RUN mkdir -p /app/uploads

# Cấu hình ứng dụng
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Sức khỏe kiểm tra
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Mở cổng ứng dụng
EXPOSE 8080

# Điểm vào
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
