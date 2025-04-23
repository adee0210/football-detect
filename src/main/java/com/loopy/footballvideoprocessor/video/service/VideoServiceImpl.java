package com.loopy.footballvideoprocessor.video.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.common.exception.ResourceNotFoundException;
import com.loopy.footballvideoprocessor.messaging.dto.VideoProcessingMessage;
import com.loopy.footballvideoprocessor.messaging.producer.VideoProcessingProducer;
import com.loopy.footballvideoprocessor.user.model.User;
import com.loopy.footballvideoprocessor.user.repository.UserRepository;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.dto.VideoUploadRequest;
import com.loopy.footballvideoprocessor.video.dto.YoutubeVideoRequest;
import com.loopy.footballvideoprocessor.video.mapper.VideoMapper;
import com.loopy.footballvideoprocessor.video.model.Video;
import com.loopy.footballvideoprocessor.video.model.VideoStatus;
import com.loopy.footballvideoprocessor.video.model.VideoType;
import com.loopy.footballvideoprocessor.video.repository.VideoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final R2StorageService r2StorageService;
    private final VideoMapper videoMapper;
    private final UserRepository userRepository;
    private final VideoProcessingProducer videoProcessingProducer;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VideoDto> getAllVideos(int page, int size) {
        log.debug("Lấy tất cả video, trang: {}, kích thước: {}", page, size);

        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<Video> videos = videoRepository.findAllByUser(currentUser, pageable);

        return createPagedResponse(videos);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VideoDto> getVideosByType(VideoType videoType, int page, int size) {
        log.debug("Lấy video theo loại: {}, trang: {}, kích thước: {}", videoType, page, size);

        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<Video> videos = videoRepository.findAllByUserAndVideoType(currentUser, videoType, pageable);

        return createPagedResponse(videos);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoDto getVideo(UUID id) {
        log.debug("Lấy thông tin video với id: {}", id);

        Video video = getVideoOrThrow(id);
        checkVideoOwnership(video);

        return videoMapper.toDto(video);
    }

    @Override
    @Transactional
    public VideoDto uploadVideo(VideoUploadRequest videoUploadRequest) {
        log.debug("Tải lên video mới: {}", videoUploadRequest.getTitle());

        // try {
        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();

        // Tải video lên Cloudflare R2
        // Can throw StorageException
        String videoKey = r2StorageService.uploadVideo(videoUploadRequest.getFile(), currentUser.getId());

        // Tạo đối tượng Video mới
        Video video = new Video();
        video.setUser(currentUser);
        video.setTitle(videoUploadRequest.getTitle());
        video.setDescription(videoUploadRequest.getDescription());
        video.setVideoType(VideoType.UPLOADED);
        video.setFilePath(videoKey);
        video.setFileSize(videoUploadRequest.getFile().getSize());
        video.setIsDownloadable(videoUploadRequest.getIsDownloadable());
        video.setStatus(VideoStatus.PENDING);

        // Lưu thông tin video vào cơ sở dữ liệu
        // Can throw DataAccessException
        Video savedVideo = videoRepository.save(video);

        // Gửi thông báo xử lý video đến RabbitMQ
        // Can throw MessagingException
        sendVideoProcessingMessage(savedVideo, videoKey);

        return videoMapper.toDto(savedVideo);
        // } catch (Exception e) {
        // log.error("Lỗi khi tải lên video: {}", e.getMessage(), e);
        // throw new RuntimeException("Không thể tải lên video: " + e.getMessage());
        // }
    }

    @Override
    @Transactional
    public VideoDto addYoutubeVideo(YoutubeVideoRequest youtubeVideoRequest) {
        log.debug("Thêm video YouTube: {}", youtubeVideoRequest.getYoutubeUrl());

        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();

        // Tạo đối tượng Video mới
        Video video = new Video();
        video.setUser(currentUser);
        video.setTitle(youtubeVideoRequest.getTitle());
        video.setDescription(youtubeVideoRequest.getDescription());
        video.setVideoType(VideoType.YOUTUBE);
        video.setYoutubeUrl(youtubeVideoRequest.getYoutubeUrl());
        video.setYoutubeVideoId(extractYoutubeId(youtubeVideoRequest.getYoutubeUrl()));
        video.setStatus(VideoStatus.COMPLETED); // YouTube videos không cần xử lý

        // Lưu thông tin video vào cơ sở dữ liệu
        Video savedVideo = videoRepository.save(video);

        return videoMapper.toDto(savedVideo);
    }

    @Override
    @Transactional
    public VideoDto updateVideo(UUID id, VideoDto videoDto) {
        log.debug("Cập nhật video với id: {}", id);

        Video video = getVideoOrThrow(id);
        checkVideoOwnership(video);

        // Cập nhật thông tin video
        videoMapper.updateEntityFromDto(video, videoDto);

        // Lưu thông tin video vào cơ sở dữ liệu
        Video updatedVideo = videoRepository.save(video);

        return videoMapper.toDto(updatedVideo);
    }

    @Override
    @Transactional
    public void deleteVideo(UUID id) {
        log.debug("Xóa video với id: {}", id);

        // try {
        Video video = getVideoOrThrow(id);
        checkVideoOwnership(video);

        // Nếu là video tải lên, cần xóa file từ R2
        if (video.getVideoType() == VideoType.UPLOADED) {
            // Xóa video gốc
            // Can throw StorageException
            if (video.getFilePath() != null) {
                r2StorageService.deleteFile(video.getFilePath());
            }

            // Xóa video đã xử lý
            // Can throw StorageException
            if (video.getProcessedPath() != null) {
                r2StorageService.deleteFile(video.getProcessedPath());
            }

            // Xóa thumbnail
            // Can throw StorageException
            if (video.getThumbnailPath() != null) {
                r2StorageService.deleteFile(video.getThumbnailPath());
            }
        }

        // Xóa thông tin video từ cơ sở dữ liệu
        // Can throw DataAccessException
        videoRepository.delete(video);

        // return ApiResponse.success("Video đã được xóa thành công", null); // Remove
        // return
        // } catch (Exception e) {
        // log.error("Lỗi khi xóa video: {}", e.getMessage(), e);
        // return ApiResponse.error("Không thể xóa video: " + e.getMessage()); // Remove
        // return
        // }
    }

    /**
     * Lấy thông tin người dùng hiện tại
     * 
     * @return User
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Kiểm tra người dùng hiện tại có sở hữu video không
     * 
     * @param video Video cần kiểm tra
     * @throws AccessDeniedException nếu không có quyền
     */
    private void checkVideoOwnership(Video video) {
        User currentUser = getCurrentUser();

        if (!video.getUser().getId().equals(currentUser.getId())) {
            // throw new ResourceNotFoundException("Video", "id", video.getId().toString());
            throw new AccessDeniedException("Bạn không có quyền truy cập video này"); // Use AccessDeniedException
        }
    }

    /**
     * Lấy thông tin video theo ID
     * 
     * @param id ID của video
     * @return Video
     */
    private Video getVideoOrThrow(UUID id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", id.toString()));
    }

    /**
     * Tạo PagedResponse từ Page<Video>
     * 
     * @param videos Page<Video>
     * @return PagedResponse<VideoDto>
     */
    private PagedResponse<VideoDto> createPagedResponse(Page<Video> videos) {
        return new PagedResponse<>(
                videos.getContent().stream().map(videoMapper::toDto).toList(),
                videos.getNumber(),
                videos.getSize(),
                videos.getTotalElements(),
                videos.getTotalPages(),
                videos.isLast());
    }

    /**
     * Gửi thông báo xử lý video đến RabbitMQ
     * 
     * @param video    Video cần xử lý
     * @param videoKey Khóa của video trên R2
     */
    private void sendVideoProcessingMessage(Video video, String videoKey) {
        VideoProcessingMessage message = VideoProcessingMessage.builder()
                .videoId(video.getId())
                .userId(video.getUser().getId())
                .videoPath(video.getFilePath())
                .cloudStorageKey(videoKey)
                .outputPath("processed/" + video.getUser().getId() + "/" + video.getId() + ".mp4")
                .status(VideoStatus.PENDING)
                .progress(0)
                .timestamp(LocalDateTime.now())
                .build();

        videoProcessingProducer.sendVideoProcessingMessage(message);
    }

    /**
     * Trích xuất YouTube ID từ URL
     * 
     * @param youtubeUrl URL của video YouTube
     * @return YouTube ID
     */
    private String extractYoutubeId(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.isEmpty()) {
            return null;
        }

        // Tách ID từ YouTube URL (logic đơn giản, có thể cải thiện)
        if (youtubeUrl.contains("v=")) {
            String[] parts = youtubeUrl.split("v=");
            if (parts.length > 1) {
                String id = parts[1];
                if (id.contains("&")) {
                    id = id.substring(0, id.indexOf("&"));
                }
                return id;
            }
        } else if (youtubeUrl.contains("youtu.be/")) {
            String[] parts = youtubeUrl.split("youtu.be/");
            if (parts.length > 1) {
                String id = parts[1];
                if (id.contains("?")) {
                    id = id.substring(0, id.indexOf("?"));
                }
                return id;
            }
        }

        return null;
    }
}