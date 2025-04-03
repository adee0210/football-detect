package com.loopy.footballvideoprocessor.messaging.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.loopy.footballvideoprocessor.video.model.VideoStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoProcessingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID videoId;
    private String filePath;
    private String videoTitle;
    private String processingUrl;
    private String outputPath;
    private VideoStatus status;
    private Integer progress;
    private String message;
    private LocalDateTime createdAt;
}
