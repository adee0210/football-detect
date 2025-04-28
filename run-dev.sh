#!/bin/bash

# Script để chạy ứng dụng Spring Boot với biến môi trường từ file .env

ENV_FILE=".env"

# Kiểm tra xem file .env có tồn tại không
if [ ! -f "$ENV_FILE" ]; then
    echo "Lỗi: File '$ENV_FILE' không tìm thấy."
    echo "Hãy tạo file '$ENV_FILE' từ '.env.example' và điền các giá trị cần thiết."
    exit 1
fi

# Sử dụng set -a để tự động export các biến được định nghĩa khi source file
# Tương thích với các shell như bash, zsh
set -a
# Nạp các biến từ file .env
source "$ENV_FILE"
# Tắt chế độ tự động export
set +a

echo "Đã nạp biến môi trường từ $ENV_FILE. Bắt đầu chạy ứng dụng..."

# Chạy lệnh Gradle bootRun, truyền các tham số dòng lệnh (nếu có)
./gradlew bootRun "$@"

# Kiểm tra mã thoát của lệnh Gradle
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo "Lỗi: Lệnh Gradle bootRun thất bại với mã thoát $EXIT_CODE."
    exit $EXIT_CODE
fi

echo "Ứng dụng đã dừng."