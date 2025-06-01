package com.loopy.footballvideoprocessor.user.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loopy.footballvideoprocessor.common.dto.ApiResponse;
import com.loopy.footballvideoprocessor.user.dto.LoginRequest;
import com.loopy.footballvideoprocessor.user.dto.SignupRequest;
import com.loopy.footballvideoprocessor.user.dto.TokenResponse;
import com.loopy.footballvideoprocessor.user.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", tokenResponse));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        TokenResponse tokenResponse = authService.signup(signupRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", tokenResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @RequestParam(required = false) String refreshToken,
            @RequestBody(required = false) Map<String, String> requestBody) {

        // Lấy refreshToken từ RequestParam hoặc RequestBody
        String token = refreshToken;
        if (token == null && requestBody != null && requestBody.containsKey("refreshToken")) {
            token = requestBody.get("refreshToken");
        }

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Refresh token không được cung cấp", null, null));
        }

        TokenResponse tokenResponse = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", tokenResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success("Kết quả kiểm tra token", isValid));
    }

}
