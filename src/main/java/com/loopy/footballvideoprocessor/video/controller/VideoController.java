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

import com.loopy.footballvideoprocessor.common.dto.ApiResponse;
import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.dto.VideoUploadRequest;
import com.loopy.footballvideoprocessor.video.dto.YoutubeVideoRequest;
import com.loopy.footballvideoprocessor.video.model.VideoType;
import com.loopy.footballvideoprocessor.video.service.VideoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Video", description = "API quản lý video")
public class VideoController {

    private final VideoService videoService;

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
}
