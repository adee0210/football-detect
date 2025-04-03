package com.loopy.footballvideoprocessor.messaging.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.loopy.footballvideoprocessor.video.model.VideoStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoProcessingMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private UUID videoId;
    private UUID userId;
    private String videoPath;
    private String cloudStorageKey;
    private String outputPath;
    private VideoStatus status;
    private Integer progress;
    private String message;
    private LocalDateTime timestamp;
    
    @Builder.Default
    private boolean highPriority = false;
    
    @Builder.Default
    private int maxRetries = 3;
    
    @Builder.Default
    private int retryCount = 0;
}
