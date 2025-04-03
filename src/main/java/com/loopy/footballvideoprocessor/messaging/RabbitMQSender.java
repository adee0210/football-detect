package com.loopy.footballvideoprocessor.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.loopy.footballvideoprocessor.messaging.dto.VideoProcessingMessage;
import com.loopy.footballvideoprocessor.video.model.VideoStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQSender {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.video-processing}")
    private String videoProcessingExchange;

    @Value("${rabbitmq.routing-key.video-processing}")
    private String videoProcessingRoutingKey;

    /**
     * Gửi thông báo xử lý video đến RabbitMQ
     * 
     * @param videoProcessingMessage Thông tin xử lý video
     */
    public void sendVideoProcessingMessage(VideoProcessingMessage videoProcessingMessage) {
        try {
            log.info("Gửi thông báo xử lý video: {}", videoProcessingMessage.getVideoId());

            rabbitTemplate.convertAndSend(
                    videoProcessingExchange,
                    videoProcessingRoutingKey,
                    videoProcessingMessage);

            log.info("Đã gửi thông báo xử lý video thành công: {}", videoProcessingMessage.getVideoId());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo xử lý video: {}", e.getMessage(), e);

            // Cập nhật trạng thái video sang lỗi
            videoProcessingMessage.setStatus(VideoStatus.ERROR);
            // TODO: Cập nhật trạng thái video trong cơ sở dữ liệu
        }
    }
}