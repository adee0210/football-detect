package com.loopy.footballvideoprocessor.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO chứa thông tin thống kê về video
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoStats {
    // Tổng số video
    private long totalVideos;

    // Số video được tải lên
    private long uploadedVideos;

    // Số video từ YouTube
    private long youtubeVideos;

    // Phân phối video theo trạng thái
    private List<StatusDistribution> statusDistribution;

    /**
     * Thông tin phân phối theo trạng thái
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusDistribution {
        private String status;
        private long count;
    }

    /**
     * Thông tin tóm tắt về video
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VideoSummary {
        private UUID id;
        private UUID userId;
        private String username;
        private String title;
        private String description;
        private String videoType;
        private String status;
        private Long fileSize;
        private Integer duration;
        private LocalDateTime createdAt;
    }
}
