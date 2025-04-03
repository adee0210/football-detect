package com.loopy.footballvideoprocessor.video.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.loopy.footballvideoprocessor.video.model.VideoStatus;
import com.loopy.footballvideoprocessor.video.model.VideoType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private UUID id;
    private UUID userId;
    private String username;
    private String title;
    private String description;
    private VideoType videoType;

    // Fields for UPLOADED videos
    private String filePath;
    private Long fileSize;
    private Integer duration;
    private String thumbnailPath;
    private String processedPath;

    // Fields for YOUTUBE videos
    private String youtubeUrl;
    private String youtubeVideoId;

    private Boolean isDownloadable;
    private VideoStatus status;
    private Integer progress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
