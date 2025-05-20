package com.loopy.footballvideoprocessor.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO chứa thông tin thống kê về người dùng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStats {
    // Tổng số người dùng
    private long totalUsers;

    // Số người dùng đang hoạt động
    private long activeUsers;

    // Số người dùng chưa kích hoạt
    private long inactiveUsers;

    // Phân phối người dùng theo vai trò
    private List<RoleDistribution> roleDistribution;

    /**
     * Thông tin phân phối theo vai trò
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleDistribution {
        private String roleName;
        private long count;
    }

    /**
     * Thông tin tóm tắt về người dùng
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSummary {
        private UUID id;
        private String username;
        private String email;
        private String name;
        private boolean enabled;
        private List<String> roles;
        private LocalDateTime createdAt;
        private long videoCount;
        private long storageUsed;
    }
}
