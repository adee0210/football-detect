package com.loopy.footballvideoprocessor.user.service;

import com.loopy.footballvideoprocessor.user.dto.LoginRequest;
import com.loopy.footballvideoprocessor.user.dto.SignupRequest;
import com.loopy.footballvideoprocessor.user.dto.TokenResponse;

public interface AuthService {

    TokenResponse login(LoginRequest loginRequest);

    TokenResponse signup(SignupRequest signupRequest);

    TokenResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    boolean validateToken(String token);
}
