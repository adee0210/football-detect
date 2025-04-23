package com.loopy.footballvideoprocessor.common.exception;

/**
 * Exception cho các lỗi liên quan đến việc gửi hoặc nhận message (ví dụ:
 * RabbitMQ).
 */
public class MessagingException extends RuntimeException {

    public MessagingException(String message) {
        super(message);
    }

    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}