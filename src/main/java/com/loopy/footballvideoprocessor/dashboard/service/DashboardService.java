package com.loopy.footballvideoprocessor.dashboard.service;

import java.util.UUID;

import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.dashboard.dto.DashboardSummary;
import com.loopy.footballvideoprocessor.dashboard.dto.UserStats;
import com.loopy.footballvideoprocessor.dashboard.dto.VideoStats;
import com.loopy.footballvideoprocessor.user.dto.UserDTO;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;

/**
 * Service xử lý các thao tác liên quan đến Admin Dashboard
 */
public interface DashboardService {

    /**
     * Lấy thông tin tổng quan cho Admin Dashboard
     * 
     * @return Thông tin tổng quan
     */
    DashboardSummary getDashboardSummary();

    /**
     * Lấy thống kê người dùng
     * 
     * @return Thống kê người dùng
     */
    UserStats getUserStats();

    /**
     * Lấy thống kê video
     * 
     * @return Thống kê video
     */
    VideoStats getVideoStats();

    /**
     * Lấy danh sách người dùng phân trang
     * 
     * @param page Số trang
     * @param size Kích thước trang
     * @return Danh sách người dùng
     */
    PagedResponse<UserDTO> getUsers(int page, int size);

    /**
     * Lấy thông tin người dùng
     * 
     * @param id ID của người dùng
     * @return Thông tin người dùng
     */
    UserDTO getUserById(UUID id);

    /**
     * Vô hiệu hóa người dùng
     * 
     * @param id ID của người dùng
     * @return Thông tin người dùng sau khi vô hiệu hóa
     */
    UserDTO disableUser(UUID id);

    /**
     * Kích hoạt người dùng
     * 
     * @param id ID của người dùng
     * @return Thông tin người dùng sau khi kích hoạt
     */
    UserDTO enableUser(UUID id);

    /**
     * Lấy danh sách video phân trang
     * 
     * @param page Số trang
     * @param size Kích thước trang
     * @return Danh sách video
     */
    PagedResponse<VideoDto> getVideos(int page, int size);

    /**
     * Lấy thông tin video
     * 
     * @param id ID của video
     * @return Thông tin video
     */
    VideoDto getVideoById(UUID id);

    /**
     * Xóa video
     * 
     * @param id ID của video
     */
    void deleteVideo(UUID id);
}
