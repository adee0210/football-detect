package com.loopy.footballvideoprocessor.security.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.loopy.footballvideoprocessor.security.jwt.JwtAuthenticationFilter;
import com.loopy.footballvideoprocessor.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/videos/test/**").permitAll()
                        .anyRequest().authenticated());

        // Thêm filter JWT trước filter xác thực username/password
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = corsConfigurationSource();
        return new CorsFilter(source);
    }

    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // Thêm các origin cho môi trường phát triển
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:5173");

        // Đọc và xử lý các origin được phép từ biến môi trường
        try {
            String allowedOriginsEnv = System.getenv("ALLOWED_ORIGINS");
            if (allowedOriginsEnv == null || allowedOriginsEnv.trim().isEmpty()) {
                allowedOriginsEnv = "https://footballvideo.example.com";
            }

            for (String origin : allowedOriginsEnv.split(",")) {
                if (!origin.trim().isEmpty()) {
                    config.addAllowedOrigin(origin.trim());
                    log.info("CORS: Thêm origin được phép: {}", origin.trim());
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý ALLOWED_ORIGINS từ biến môi trường", e);
            // Thêm origin mặc định nếu có lỗi
            config.addAllowedOrigin("https://footballvideo.example.com");
            log.info("CORS: Sử dụng origin mặc định: https://footballvideo.example.com");
        }

        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization",
                "X-Requested-With", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        log.info("CORS đã được cấu hình thành công");
        return source;
    }
}
