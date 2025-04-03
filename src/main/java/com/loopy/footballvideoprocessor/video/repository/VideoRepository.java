package com.loopy.footballvideoprocessor.video.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.loopy.footballvideoprocessor.user.model.User;
import com.loopy.footballvideoprocessor.video.model.Video;
import com.loopy.footballvideoprocessor.video.model.VideoStatus;
import com.loopy.footballvideoprocessor.video.model.VideoType;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
    
    Page<Video> findAllByUser(User user, Pageable pageable);
    
    List<Video> findAllByStatus(VideoStatus status);
    
    Page<Video> findAllByUserAndStatus(User user, VideoStatus status, Pageable pageable);
    
    Page<Video> findAllByUserAndVideoType(User user, VideoType videoType, Pageable pageable);
    
    @Query("SELECT v FROM Video v WHERE v.title LIKE %:keyword% OR v.description LIKE %:keyword%")
    Page<Video> search(String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(v) FROM Video v WHERE v.user = :user")
    Long countByUser(User user);
    
    @Query("SELECT COUNT(v) FROM Video v WHERE v.user = :user AND v.videoType = :videoType")
    Long countByUserAndVideoType(User user, VideoType videoType);
    
    @Query("SELECT SUM(v.fileSize) FROM Video v WHERE v.user = :user")
    Long sumFileSizeByUser(User user);
}
