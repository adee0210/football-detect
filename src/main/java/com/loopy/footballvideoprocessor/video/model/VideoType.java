package com.loopy.footballvideoprocessor.video.model;

/**
 * Enum định nghĩa các loại video trong hệ thống.
 */
public enum VideoType {
    /**
     * Video được người dùng tải lên trực tiếp vào hệ thống.
     */
    UPLOADED,

    /**
     * Video từ YouTube, chỉ lưu trữ URL.
     */
    YOUTUBE
}