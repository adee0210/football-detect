package com.loopy.footballvideoprocessor.messaging.producer;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.loopy.footballvideoprocessor.common.exception.MessagingException;
import com.loopy.footballvideoprocessor.messaging.dto.VideoProcessingMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.video-processing}")
    private String videoProcessingExchange;

    @Value("${rabbitmq.routing-key.video-processing}")
    private String videoProcessingRoutingKey;

    public void sendVideoProcessingMessage(VideoProcessingMessage message) {
        try {
            if (message.getYoutubeUrl() != null) {
                log.info("Gửi YouTube video processing message với videoId: {}, youtubeUrl: {}",
                        message.getVideoId(), message.getYoutubeUrl());
            } else {
                log.info("Gửi video processing message với videoId: {}", message.getVideoId());
            }

            rabbitTemplate.convertAndSend(videoProcessingExchange, videoProcessingRoutingKey, message);
            log.info("Message đã được gửi thành công");
        } catch (AmqpException e) {
            log.error("Lỗi khi gửi video processing message: {}", e.getMessage(), e);
            throw new MessagingException("Không thể gửi message xử lý video", e);
        }
    }
}
