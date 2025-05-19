package com.loopy.footballvideoprocessor.video.service;

import java.util.Map;
import java.util.UUID;

import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.dto.VideoUploadRequest;
import com.loopy.footballvideoprocessor.video.dto.YoutubeVideoRequest;
import com.loopy.footballvideoprocessor.video.model.VideoType;

public interface VideoService {

    /**
     * Lấy danh sách tất cả video của người dùng hiện tại
     * 
     * @param page Số trang
     * @param size Kích thước trang
     * @return Danh sách video
     */
    PagedResponse<VideoDto> getAllVideos(int page, int size);

    /**
     * Lấy danh sách video theo loại
     * 
     * @param videoType Loại video
     * @param page      Số trang
     * @param size      Kích thước trang
     * @return Danh sách video
     */
    PagedResponse<VideoDto> getVideosByType(VideoType videoType, int page, int size);

    /**
     * Lấy thông tin video
     * 
     * @param id ID của video
     * @return Thông tin video
     */
    VideoDto getVideo(UUID id);

    /**
     * Tải lên video mới
     * 
     * @param videoUploadRequest Thông tin video tải lên
     * @return Thông tin video đã tải lên
     */
    VideoDto uploadVideo(VideoUploadRequest videoUploadRequest);

    /**
     * Thêm video YouTube
     * 
     * @param youtubeVideoRequest Thông tin video YouTube
     * @return Thông tin video đã thêm
     */
    VideoDto addYoutubeVideo(YoutubeVideoRequest youtubeVideoRequest);

    /**
     * Cập nhật thông tin video
     * 
     * @param id       ID của video
     * @param videoDto Thông tin video cần cập nhật
     * @return Thông tin video đã cập nhật
     */
    VideoDto updateVideo(UUID id, VideoDto videoDto);

    /**
     * Xóa video
     * 
     * @param id ID của video
     */
    void deleteVideo(UUID id);

    /**
     * Lấy thông tin trạng thái xử lý gần nhất của video
     * 
     * @param id ID của video
     * @return Thông tin trạng thái xử lý
     */
    Map<String, Object> getLatestProcessingStatus(UUID id);
}
