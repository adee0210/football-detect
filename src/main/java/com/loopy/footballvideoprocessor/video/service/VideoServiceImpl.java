package com.loopy.footballvideoprocessor.video.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.loopy.footballvideoprocessor.common.dto.ApiResponse;
import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.dto.VideoUploadRequest;
import com.loopy.footballvideoprocessor.video.dto.YoutubeVideoRequest;
import com.loopy.footballvideoprocessor.video.model.VideoType;
import com.loopy.footballvideoprocessor.video.repository.VideoProcessingStatusRepository;
import com.loopy.footballvideoprocessor.video.repository.VideoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final VideoProcessingStatusRepository videoProcessingStatusRepository;

    @Override
    public PagedResponse<VideoDto> getAllVideos(int page, int size) {
        log.debug("Lấy tất cả video, trang: {}, kích thước: {}", page, size);
        // Implementation tạm thời
        return new PagedResponse<>(new ArrayList<>(), page, size, 0, 0, true);
    }

    @Override
    public PagedResponse<VideoDto> getVideosByType(VideoType videoType, int page, int size) {
        log.debug("Lấy video theo loại: {}, trang: {}, kích thước: {}", videoType, page, size);
        // Implementation tạm thời
        return new PagedResponse<>(new ArrayList<>(), page, size, 0, 0, true);
    }

    @Override
    public VideoDto getVideo(UUID id) {
        log.debug("Lấy thông tin video với id: {}", id);
        // Implementation tạm thời
        return new VideoDto();
    }

    @Override
    public VideoDto uploadVideo(VideoUploadRequest videoUploadRequest) {
        log.debug("Tải lên video mới: {}", videoUploadRequest.getTitle());
        // Implementation tạm thời
        return new VideoDto();
    }

    @Override
    public VideoDto addYoutubeVideo(YoutubeVideoRequest youtubeVideoRequest) {
        log.debug("Thêm video YouTube: {}", youtubeVideoRequest.getYoutubeUrl());
        // Implementation tạm thời
        return new VideoDto();
    }

    @Override
    public VideoDto updateVideo(UUID id, VideoDto videoDto) {
        log.debug("Cập nhật video với id: {}", id);
        // Implementation tạm thời
        return videoDto;
    }

    @Override
    public ApiResponse<Void> deleteVideo(UUID id) {
        log.debug("Xóa video với id: {}", id);
        // Implementation tạm thời
        return ApiResponse.success("Video đã được xóa thành công", null);
    }
} 