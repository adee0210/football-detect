package com.loopy.footballvideoprocessor.dashboard.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loopy.footballvideoprocessor.common.dto.ApiResponse;
import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.dashboard.dto.DashboardSummary;
import com.loopy.footballvideoprocessor.dashboard.dto.UserStats;
import com.loopy.footballvideoprocessor.dashboard.dto.VideoStats;
import com.loopy.footballvideoprocessor.dashboard.service.DashboardService;
import com.loopy.footballvideoprocessor.user.dto.UserDTO;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "API quản lý Admin Dashboard")
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Lấy thông tin tổng quan", description = "Lấy thông tin tổng quan cho Admin Dashboard")
    public ResponseEntity<ApiResponse<DashboardSummary>> getDashboardSummary() {
        log.debug("REST request để lấy thông tin tổng quan Admin Dashboard");
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboardSummary()));
    }

    @GetMapping("/users/stats")
    @Operation(summary = "Lấy thống kê người dùng", description = "Lấy thống kê chi tiết về người dùng")
    public ResponseEntity<ApiResponse<UserStats>> getUserStats() {
        log.debug("REST request để lấy thống kê người dùng");
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getUserStats()));
    }

    @GetMapping("/videos/stats")
    @Operation(summary = "Lấy thống kê video", description = "Lấy thống kê chi tiết về video")
    public ResponseEntity<ApiResponse<VideoStats>> getVideoStats() {
        log.debug("REST request để lấy thống kê video");
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getVideoStats()));
    }

    @GetMapping("/users")
    @Operation(summary = "Lấy danh sách người dùng", description = "Lấy danh sách người dùng phân trang")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("REST request để lấy danh sách người dùng, trang: {}, kích thước: {}", page, size);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getUsers(page, size)));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Lấy thông tin người dùng", description = "Lấy thông tin chi tiết của một người dùng")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable UUID id) {
        log.debug("REST request để lấy thông tin người dùng với id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getUserById(id)));
    }

    @PutMapping("/users/{id}/disable")
    @Operation(summary = "Vô hiệu hóa người dùng", description = "Vô hiệu hóa tài khoản người dùng")
    public ResponseEntity<ApiResponse<UserDTO>> disableUser(@PathVariable UUID id) {
        log.debug("REST request để vô hiệu hóa người dùng với id: {}", id);
        return ResponseEntity
                .ok(ApiResponse.success("Đã vô hiệu hóa người dùng thành công", dashboardService.disableUser(id)));
    }

    @PutMapping("/users/{id}/enable")
    @Operation(summary = "Kích hoạt người dùng", description = "Kích hoạt tài khoản người dùng")
    public ResponseEntity<ApiResponse<UserDTO>> enableUser(@PathVariable UUID id) {
        log.debug("REST request để kích hoạt người dùng với id: {}", id);
        return ResponseEntity
                .ok(ApiResponse.success("Đã kích hoạt người dùng thành công", dashboardService.enableUser(id)));
    }

    @GetMapping("/videos")
    @Operation(summary = "Lấy danh sách video", description = "Lấy danh sách video phân trang")
    public ResponseEntity<ApiResponse<PagedResponse<VideoDto>>> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("REST request để lấy danh sách video, trang: {}, kích thước: {}", page, size);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getVideos(page, size)));
    }

    @GetMapping("/videos/{id}")
    @Operation(summary = "Lấy thông tin video", description = "Lấy thông tin chi tiết của một video")
    public ResponseEntity<ApiResponse<VideoDto>> getVideoById(@PathVariable UUID id) {
        log.debug("REST request để lấy thông tin video với id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getVideoById(id)));
    }

    @DeleteMapping("/videos/{id}")
    @Operation(summary = "Xóa video", description = "Xóa một video khỏi hệ thống")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable UUID id) {
        log.debug("REST request để xóa video với id: {}", id);
        dashboardService.deleteVideo(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa video thành công", null));
    }
}
