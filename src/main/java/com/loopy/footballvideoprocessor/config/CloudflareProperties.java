package com.loopy.footballvideoprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "cloudflare")
@Data
public class CloudflareProperties {

    private R2 r2;

    @Data
    public static class R2 {
        private String accessKey;
        private String secretKey;
        private String endpoint;
        private String region = "auto";
        private String bucketName;
    }
}