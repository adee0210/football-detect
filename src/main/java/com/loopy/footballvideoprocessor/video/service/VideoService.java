package com.loopy.footballvideoprocessor.video.service;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;

import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.common.exception.ResourceNotFoundException;
import com.loopy.footballvideoprocessor.common.exception.StorageException;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.dto.VideoUploadRequest;
import com.loopy.footballvideoprocessor.video.dto.YoutubeVideoRequest;
import com.loopy.footballvideoprocessor.video.model.VideoType;

public interface VideoService {

    /**
     * Lấy tất cả video của người dùng hiện tại
     */
    PagedResponse<VideoDto> getAllVideos(int page, int size);

    /**
     * Lấy video theo loại (UPLOADED hoặc YOUTUBE)
     */
    PagedResponse<VideoDto> getVideosByType(VideoType videoType, int page, int size);

    /**
     * Lấy thông tin chi tiết của một video
     */
    VideoDto getVideo(UUID id);

    /**
     * Tải lên video mới
     */
    VideoDto uploadVideo(VideoUploadRequest videoUploadRequest);

    /**
     * Thêm video YouTube
     */
    VideoDto addYoutubeVideo(YoutubeVideoRequest youtubeVideoRequest);

    /**
     * Cập nhật thông tin video
     */
    VideoDto updateVideo(UUID id, VideoDto videoDto);

    /**
     * Xóa video theo ID
     * 
     * @param id ID của video
     * @throws ResourceNotFoundException nếu không tìm thấy video
     * @throws AccessDeniedException     nếu không có quyền
     * @throws StorageException          nếu lỗi xóa file trên R2
     * @throws DataAccessException       nếu lỗi xóa trong DB
     */
    void deleteVideo(UUID id);
}
