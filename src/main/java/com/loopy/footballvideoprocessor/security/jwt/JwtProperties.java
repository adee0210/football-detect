package com.loopy.footballvideoprocessor.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app.auth.jwt")
@Data
public class JwtProperties {

    private String secret;
    private long expiration;
    private long refreshExpiration;
}
