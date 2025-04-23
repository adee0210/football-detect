package com.loopy.footballvideoprocessor.common.exception;

/**
 * Exception gốc cho các lỗi liên quan đến thao tác lưu trữ (ví dụ: Cloudflare
 * R2, S3).
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}