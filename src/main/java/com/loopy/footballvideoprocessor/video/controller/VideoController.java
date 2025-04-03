package com.loopy.footballvideoprocessor.video.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.loopy.footballvideoprocessor.common.dto.ApiResponse;
import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.dto.VideoUploadRequest;
import com.loopy.footballvideoprocessor.video.dto.YoutubeVideoRequest;
import com.loopy.footballvideoprocessor.video.model.VideoType;
import com.loopy.footballvideoprocessor.video.service.R2StorageService;
import com.loopy.footballvideoprocessor.video.service.VideoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Video", description = "API quản lý video")
@Slf4j
public class VideoController {

    private final VideoService videoService;
    private final R2StorageService r2StorageService;

    @Operation(summary = "Lấy danh sách tất cả video của người dùng")
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<VideoDto>> getAllVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(videoService.getAllVideos(page, size));
    }

    @Operation(summary = "Lấy danh sách video được tải lên của người dùng")
    @GetMapping("/uploaded")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<VideoDto>> getUploadedVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(videoService.getVideosByType(VideoType.UPLOADED, page, size));
    }

    @Operation(summary = "Lấy danh sách video YouTube của người dùng")
    @GetMapping("/youtube")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<VideoDto>> getYoutubeVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(videoService.getVideosByType(VideoType.YOUTUBE, page, size));
    }

    @Operation(summary = "Lấy thông tin video theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<VideoDto> getVideo(@PathVariable UUID id) {
        return ResponseEntity.ok(videoService.getVideo(id));
    }

    @Operation(summary = "Tải lên video mới")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<VideoDto> uploadVideo(@Valid @ModelAttribute VideoUploadRequest videoUploadRequest) {
        return new ResponseEntity<>(videoService.uploadVideo(videoUploadRequest), HttpStatus.CREATED);
    }

    @Operation(summary = "Thêm video YouTube")
    @PostMapping("/youtube")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<VideoDto> addYoutubeVideo(@Valid @RequestBody YoutubeVideoRequest youtubeVideoRequest) {
        return new ResponseEntity<>(videoService.addYoutubeVideo(youtubeVideoRequest), HttpStatus.CREATED);
    }

    @Operation(summary = "Cập nhật thông tin video")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<VideoDto> updateVideo(
            @PathVariable UUID id,
            @Valid @RequestBody VideoDto videoDto) {
        return ResponseEntity.ok(videoService.updateVideo(id, videoDto));
    }

    @Operation(summary = "Xóa video")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable UUID id) {
        return ResponseEntity.ok(videoService.deleteVideo(id));
    }
    
    @Operation(summary = "Tạo URL tạm thời để truy cập video")
    @GetMapping("/{id}/presigned-url")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> generatePresignedUrl(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "15") int expirationInMinutes) {
        
        try {
            // TODO: Lấy thông tin video từ cơ sở dữ liệu để có được key

            // Giả sử chúng ta biết key của video (trong thực tế sẽ lấy từ DB)
            String videoKey = "videos/" + id.toString() + "/original.mp4";
            
            // Tạo presigned URL
            String presignedUrl = r2StorageService.generatePresignedUrl(videoKey, expirationInMinutes);
            
            return ResponseEntity.ok(ApiResponse.success("URL tạm thời đã được tạo thành công", presignedUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể tạo URL tạm thời: " + e.getMessage()));
        }
    }
    
    // API test không yêu cầu xác thực để upload video trực tiếp lên R2
    @Operation(summary = "API test để tải lên video trực tiếp lên Cloudflare R2 (Không yêu cầu xác thực)")
    @PostMapping(value = "/test/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> testUploadVideoToR2(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", defaultValue = "00000000-0000-0000-0000-000000000000") UUID userId) {
        
        try {
            log.info("Test tải lên video trực tiếp lên R2: {}, userId: {}", file.getOriginalFilename(), userId);
            
            // Tải video lên Cloudflare R2
            String videoKey = r2StorageService.uploadVideo(file, userId);
            
            // Tạo presigned URL để xem video
            String presignedUrl = r2StorageService.generatePresignedUrl(videoKey, 30);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Video đã được tải lên thành công",
                    "Key: " + videoKey + "\nPresigned URL: " + presignedUrl));
        } catch (Exception e) {
            log.error("Lỗi khi test tải lên video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể tải lên video: " + e.getMessage()));
        }
    }
    
    // API test không yêu cầu xác thực để tạo presigned URL
    @Operation(summary = "API test để tạo URL tạm thời (Không yêu cầu xác thực)")
    @GetMapping("/test/presigned-url")
    public ResponseEntity<ApiResponse<String>> testGeneratePresignedUrl(
            @RequestParam("key") String key,
            @RequestParam(value = "expirationInMinutes", defaultValue = "30") int expirationInMinutes) {
        
        try {
            log.info("Test tạo URL tạm thời cho key: {}, thời hạn: {} phút", key, expirationInMinutes);
            
            // Tạo presigned URL
            String presignedUrl = r2StorageService.generatePresignedUrl(key, expirationInMinutes);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "URL tạm thời đã được tạo thành công",
                    presignedUrl));
        } catch (Exception e) {
            log.error("Lỗi khi test tạo URL tạm thời: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể tạo URL tạm thời: " + e.getMessage()));
        }
    }
}
