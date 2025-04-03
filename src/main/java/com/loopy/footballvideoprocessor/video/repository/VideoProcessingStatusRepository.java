package com.loopy.footballvideoprocessor.video.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loopy.footballvideoprocessor.video.model.Video;
import com.loopy.footballvideoprocessor.video.model.VideoProcessingStatusEntity;
import com.loopy.footballvideoprocessor.video.model.VideoStatus;

@Repository
public interface VideoProcessingStatusRepository extends JpaRepository<VideoProcessingStatusEntity, UUID> {

    List<VideoProcessingStatusEntity> findAllByVideo(Video video);

    List<VideoProcessingStatusEntity> findAllByVideoOrderByCreatedAtDesc(Video video);

    Optional<VideoProcessingStatusEntity> findFirstByVideoOrderByCreatedAtDesc(Video video);

    List<VideoProcessingStatusEntity> findAllByStatus(VideoStatus status);
}