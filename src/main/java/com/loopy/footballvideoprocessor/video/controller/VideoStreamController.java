package com.loopy.footballvideoprocessor.video.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loopy.footballvideoprocessor.common.dto.ApiResponse;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.model.VideoType;
import com.loopy.footballvideoprocessor.video.service.R2StorageService;
import com.loopy.footballvideoprocessor.video.service.VideoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Video Streaming", description = "API để xem video với presigned URL")
public class VideoStreamController {

        private final VideoService videoService;
        private final R2StorageService r2StorageService;

        /**
         * Lấy URL phát video với caching
         * 
         * @param id ID của video
         * @return Thông tin URL và thời gian hết hạn
         */
        @GetMapping("/{id}/stream")
        @PreAuthorize("hasRole('USER')")
        @Operation(summary = "Lấy URL cho streaming video", description = "Trả về presigned URL với cache headers để phát video")
        public ResponseEntity<Map<String, String>> getVideoStreamUrl(
                        @Parameter(description = "ID của video") @PathVariable UUID id) {

                log.debug("Lấy URL streaming cho video ID: {}", id);

                VideoDto video = videoService.getVideo(id);
                String videoPath = video.getProcessedPath() != null
                                ? video.getProcessedPath()
                                : video.getFilePath();

                if (videoPath == null) {
                        return ResponseEntity.notFound().build();
                }

                // Cấu hình thời gian hết hạn presigned URL
                int expirationMinutes = 120; // 2 giờ
                String presignedUrl = r2StorageService.generatePresignedUrl(videoPath, expirationMinutes);

                Map<String, String> response = Map.of(
                                "url", presignedUrl,
                                "expiresAt", LocalDateTime.now().plusMinutes(expirationMinutes).toString(),
                                "videoType", video.getVideoType().toString(),
                                "title", video.getTitle());

                // Thêm cache control để frontend cache response
                return ResponseEntity.ok()
                                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePrivate())
                                .body(response);
        }

        /**
         * Lấy URL tải xuống video gốc
         */
        @GetMapping("/{id}/download/original")
        @PreAuthorize("hasRole('USER')")
        @Operation(summary = "Lấy URL tải xuống video gốc", description = "Trả về URL để tải xuống video gốc")
        public ResponseEntity<ApiResponse<String>> getOriginalVideoDownloadUrl(
                        @Parameter(description = "ID của video") @PathVariable UUID id,
                        @Parameter(description = "Thời gian hết hạn (phút)") @RequestParam(defaultValue = "30") int expirationMinutes) {

                log.debug("Lấy URL tải xuống video gốc, ID: {}", id);

                VideoDto video = videoService.getVideo(id);

                if (video.getFilePath() == null) {
                        return ResponseEntity.ok(ApiResponse.error("Video không có file gốc"));
                }

                if (video.getVideoType() == VideoType.YOUTUBE && !video.getIsDownloadable()) {
                        return ResponseEntity.ok(ApiResponse.error("Video YouTube này không được phép tải xuống"));
                }

                // Tạo presigned URL cho download (với Content-Disposition: attachment)
                String downloadUrl = r2StorageService.generateDownloadUrl(video.getFilePath(), expirationMinutes,
                                "video_" + video.getId() + "_original");

                return ResponseEntity.ok(ApiResponse.success("URL tải xuống video gốc", downloadUrl));
        }

        /**
         * Lấy URL tải xuống video đã xử lý
         */
        @GetMapping("/{id}/download/processed")
        @PreAuthorize("hasRole('USER')")
        @Operation(summary = "Lấy URL tải xuống video đã xử lý", description = "Trả về URL để tải xuống video đã xử lý")
        public ResponseEntity<ApiResponse<String>> getProcessedVideoDownloadUrl(
                        @Parameter(description = "ID của video") @PathVariable UUID id,
                        @Parameter(description = "Thời gian hết hạn (phút)") @RequestParam(defaultValue = "30") int expirationMinutes) {

                log.debug("Lấy URL tải xuống video đã xử lý, ID: {}", id);

                VideoDto video = videoService.getVideo(id);

                if (video.getProcessedPath() == null) {
                        return ResponseEntity.ok(ApiResponse.error("Video chưa có phiên bản đã xử lý"));
                }

                if (video.getVideoType() == VideoType.YOUTUBE && !video.getIsDownloadable()) {
                        return ResponseEntity.ok(ApiResponse.error("Video YouTube này không được phép tải xuống"));
                }

                // Tạo presigned URL cho download (với Content-Disposition: attachment)
                String downloadUrl = r2StorageService.generateDownloadUrl(video.getProcessedPath(), expirationMinutes,
                                "video_" + video.getId() + "_processed");

                return ResponseEntity.ok(ApiResponse.success("URL tải xuống video đã xử lý", downloadUrl));
        }

        /**
         * Làm mới URL phát video khi gần hết hạn
         * 
         * @param id ID của video
         * @return URL mới và thời gian hết hạn
         */
        @GetMapping("/{id}/stream/refresh")
        @PreAuthorize("hasRole('USER')")
        @Operation(summary = "Làm mới URL streaming", description = "Tạo mới presigned URL khi URL cũ gần hết hạn")
        public ResponseEntity<Map<String, String>> refreshVideoStreamUrl(
                        @Parameter(description = "ID của video") @PathVariable UUID id) {

                log.debug("Làm mới URL streaming cho video ID: {}", id);

                VideoDto video = videoService.getVideo(id);
                String videoPath = video.getProcessedPath() != null
                                ? video.getProcessedPath()
                                : video.getFilePath();

                if (videoPath == null) {
                        return ResponseEntity.notFound().build();
                }

                // Cấu hình thời gian hết hạn presigned URL
                int expirationMinutes = 120; // 2 giờ
                String presignedUrl = r2StorageService.generatePresignedUrl(videoPath, expirationMinutes);

                Map<String, String> response = Map.of(
                                "url", presignedUrl,
                                "expiresAt", LocalDateTime.now().plusMinutes(expirationMinutes).toString());

                return ResponseEntity.ok()
                                .cacheControl(CacheControl.noCache())
                                .body(response);
        }
}