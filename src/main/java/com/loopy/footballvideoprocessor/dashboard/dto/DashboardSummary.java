package com.loopy.footballvideoprocessor.dashboard.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin tổng quan cho Admin Dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummary {
    // Tổng số người dùng
    private long totalUsers;

    // Số người dùng mới trong 30 ngày qua
    private long newUsersLast30Days;

    // Tổng số video
    private long totalVideos;

    // Số video được xử lý trong 30 ngày qua
    private long processedVideosLast30Days;

    // Tổng dung lượng lưu trữ đã sử dụng (byte)
    private long totalStorageUsed;

    // Thống kê người dùng theo tình trạng (đang hoạt động/không hoạt động)
    private UserStats userStats;

    // Thống kê video theo loại (uploaded/youtube)
    private VideoStats videoStats;

    // Danh sách người dùng mới nhất
    private List<UserStats.UserSummary> recentUsers;

    // Danh sách video mới nhất
    private List<VideoStats.VideoSummary> recentVideos;

    // Thống kê video theo trạng thái xử lý
    private Map<String, Integer> videoStatusDistribution;
}
