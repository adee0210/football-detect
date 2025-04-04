package com.loopy.footballvideoprocessor.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopy.footballvideoprocessor.messaging.dto.VideoProcessingMessage;
import com.loopy.footballvideoprocessor.video.model.Video;
import com.loopy.footballvideoprocessor.video.model.VideoProcessingStatusEntity;
import com.loopy.footballvideoprocessor.video.repository.VideoProcessingStatusRepository;
import com.loopy.footballvideoprocessor.video.repository.VideoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingConsumer {

    private final VideoRepository videoRepository;
    private final VideoProcessingStatusRepository processingStatusRepository;

    @RabbitListener(queues = "${rabbitmq.queue.video-result}")
    @Transactional
    public void receiveVideoResult(VideoProcessingMessage message) {
        log.info("Nhận kết quả xử lý video với videoId: {}, status: {}",
                message.getVideoId(), message.getStatus());

        try {
            // Tìm video từ ID
            Video video = videoRepository.findById(message.getVideoId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Video không tồn tại với ID: " + message.getVideoId()));

            // Cập nhật trạng thái video
            video.setStatus(message.getStatus());

            // Nếu xử lý thành công, cập nhật đường dẫn đã xử lý
            if (message.getOutputPath() != null) {
                video.setProcessedPath(message.getOutputPath());
            }

            // Lưu video đã cập nhật
            videoRepository.save(video);

            // Tạo bản ghi trạng thái xử lý mới
            VideoProcessingStatusEntity processingStatus = new VideoProcessingStatusEntity();
            processingStatus.setVideo(video);
            processingStatus.setStatus(message.getStatus());
            processingStatus.setProgress(message.getProgress());
            processingStatus.setMessage(message.getMessage());

            // Lưu trạng thái xử lý mới
            processingStatusRepository.save(processingStatus);

            log.info("Đã cập nhật trạng thái video: {}", message.getStatus());
        } catch (Exception e) {
            log.error("Lỗi khi xử lý kết quả video: {}", e.getMessage(), e);
        }
    }
}
