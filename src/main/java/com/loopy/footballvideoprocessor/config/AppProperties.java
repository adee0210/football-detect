package com.loopy.footballvideoprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private Auth auth;
    private Video video;
    private Youtube youtube;

    @Data
    public static class Auth {
        private Jwt jwt;

        @Data
        public static class Jwt {
            private String secret;
            private long expiration;
            private long refreshExpiration;
        }
    }

    @Data
    public static class Video {
        private Upload upload;
        private Processing processing;

        @Data
        public static class Upload {
            private String location;
            private String maxSize;
        }

        @Data
        public static class Processing {
            private String url;
        }
    }

    @Data
    public static class Youtube {
        private String downloadUrl;
        private String apiKey;
        private int maxDurationMinutes = 60; // Giới hạn thời lượng video (phút)
        private boolean processEnabled = true; // Có xử lý video YouTube hay không
    }
}