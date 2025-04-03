package com.loopy.footballvideoprocessor.dashboard.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.loopy.footballvideoprocessor.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dashboard_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_videos", nullable = false)
    private Integer totalVideos = 0;

    @Column(name = "total_uploaded_videos", nullable = false)
    private Integer totalUploadedVideos = 0;

    @Column(name = "total_youtube_videos", nullable = false)
    private Integer totalYoutubeVideos = 0;

    @Column(name = "total_storage_used", nullable = false)
    private Long totalStorageUsed = 0L;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();
}