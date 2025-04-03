package com.loopy.footballvideoprocessor.security.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                log.debug("JWT được tìm thấy trong header Authorization, đang xử lý...");

                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    log.debug("JWT hợp lệ cho người dùng: {}", username);

                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Đã thiết lập xác thực trong SecurityContext cho người dùng: {}", username);
                } else {
                    // Nếu JWT không hợp lệ, xóa SecurityContext để đảm bảo không có xác thực nào
                    SecurityContextHolder.clearContext();
                    log.warn("JWT không hợp lệ, đã xóa SecurityContext");

                    // Ghi thông tin chi tiết về yêu cầu để gỡ lỗi
                    logRequestDetails(request);
                }
            } else {
                log.debug("Không tìm thấy JWT trong yêu cầu. URL: {}", request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("Không thể thiết lập xác thực người dùng trong SecurityContext", ex);
            // Xóa SecurityContext để đảm bảo không có xác thực nào
            SecurityContextHolder.clearContext();

            // Ghi thông tin chi tiết về yêu cầu để gỡ lỗi
            logRequestDetails(request);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void logRequestDetails(HttpServletRequest request) {
        log.debug("Request URI: {}", request.getRequestURI());
        log.debug("Request Method: {}", request.getMethod());
        log.debug("Request Remote Addr: {}", request.getRemoteAddr());
        log.debug("User-Agent: {}", request.getHeader("User-Agent"));
    }
}
