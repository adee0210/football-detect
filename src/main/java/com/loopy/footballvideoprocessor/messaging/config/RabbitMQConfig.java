package com.loopy.footballvideoprocessor.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.video-processing}")
    private String videoProcessingExchange;

    @Value("${rabbitmq.queue.video-processing}")
    private String videoProcessingQueue;

    @Value("${rabbitmq.queue.video-result}")
    private String videoResultQueue;

    @Value("${rabbitmq.routing-key.video-processing}")
    private String videoProcessingRoutingKey;

    @Value("${rabbitmq.routing-key.video-result}")
    private String videoResultRoutingKey;

    // Tạo Exchange cho video processing
    @Bean
    public DirectExchange videoProcessingExchange() {
        return new DirectExchange(videoProcessingExchange);
    }

    // Tạo Queue cho video processing
    @Bean
    public Queue videoProcessingQueue() {
        return QueueBuilder.durable(videoProcessingQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", "dead.letter.queue")
                .build();
    }

    // Tạo Queue cho video result
    @Bean
    public Queue videoResultQueue() {
        return QueueBuilder.durable(videoResultQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", "dead.letter.queue")
                .build();
    }

    // Tạo Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("dead.letter.queue").build();
    }

    // Binding video processing queue với exchange
    @Bean
    public Binding videoProcessingBinding() {
        return BindingBuilder
                .bind(videoProcessingQueue())
                .to(videoProcessingExchange())
                .with(videoProcessingRoutingKey);
    }

    // Binding video result queue với exchange
    @Bean
    public Binding videoResultBinding() {
        return BindingBuilder
                .bind(videoResultQueue())
                .to(videoProcessingExchange())
                .with(videoResultRoutingKey);
    }

    // Cấu hình message converter để chuyển đổi object thành JSON
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Cấu hình RabbitTemplate để gửi message
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
