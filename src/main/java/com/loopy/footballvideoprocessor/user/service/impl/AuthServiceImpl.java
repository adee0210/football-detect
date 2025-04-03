package com.loopy.footballvideoprocessor.user.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopy.footballvideoprocessor.common.exception.BadRequestException;
import com.loopy.footballvideoprocessor.security.jwt.JwtTokenProvider;
import com.loopy.footballvideoprocessor.user.dto.LoginRequest;
import com.loopy.footballvideoprocessor.user.dto.SignupRequest;
import com.loopy.footballvideoprocessor.user.dto.TokenResponse;
import com.loopy.footballvideoprocessor.user.dto.UserDTO;
import com.loopy.footballvideoprocessor.user.model.Role;
import com.loopy.footballvideoprocessor.user.model.User;
import com.loopy.footballvideoprocessor.user.repository.RoleRepository;
import com.loopy.footballvideoprocessor.user.repository.UserRepository;
import com.loopy.footballvideoprocessor.user.service.AuthService;
import com.loopy.footballvideoprocessor.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadRequestException("Người dùng không tồn tại"));

        UserDTO userDTO = userService.mapToDTO(user);

        return new TokenResponse(accessToken, refreshToken, tokenProvider.getJwtProperties().getExpiration(), userDTO);
    }

    @Override
    @Transactional
    public TokenResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }

        // Tạo tài khoản mới
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setName(signupRequest.getName());
        user.setEnabled(true); // Auto-enable users for now

        // Mặc định gán vai trò USER cho người dùng mới
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BadRequestException("Role ROLE_USER không tồn tại"));
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("Đã đăng ký người dùng mới: {}", savedUser.getUsername());

        // Đăng nhập sau khi đăng ký
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signupRequest.getUsername(),
                        signupRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserDTO userDTO = userService.mapToDTO(savedUser);

        return new TokenResponse(accessToken, refreshToken, tokenProvider.getJwtProperties().getExpiration(), userDTO);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new BadRequestException("Refresh token không được để trống");
            }

            if (!tokenProvider.validateToken(refreshToken)) {
                log.warn("Cố gắng làm mới với token không hợp lệ: {}", maskToken(refreshToken));
                throw new BadRequestException("Refresh token không hợp lệ hoặc đã hết hạn");
            }

            String username = tokenProvider.getUsernameFromToken(refreshToken);
            log.debug("Xử lý làm mới token cho người dùng: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Cố gắng làm mới token cho người dùng không tồn tại: {}", username);
                        return new BadRequestException("Người dùng không tồn tại");
                    });

            if (!user.getEnabled()) {
                log.warn("Cố gắng làm mới token cho tài khoản bị vô hiệu hóa: {}", username);
                throw new BadRequestException("Tài khoản đã bị vô hiệu hóa");
            }

            // Tạo authentication object mới
            Authentication authentication = tokenProvider.getAuthentication(refreshToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Tạo token mới
            String newAccessToken = tokenProvider.generateToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

            UserDTO userDTO = userService.mapToDTO(user);
            log.info("Làm mới token thành công cho người dùng: {}", username);

            return new TokenResponse(newAccessToken, newRefreshToken, tokenProvider.getJwtProperties().getExpiration(),
                    userDTO);
        } catch (BadRequestException e) {
            throw e; // Chuyển tiếp các lỗi đã xử lý
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi làm mới token", e);
            throw new BadRequestException("Không thể làm mới token, vui lòng đăng nhập lại");
        }
    }

    // Ẩn một phần token để ghi nhật ký an toàn
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }

    @Override
    public void logout(String refreshToken) {
        // Ở đây có thể thêm xử lý blacklist token nếu cần
        SecurityContextHolder.clearContext();
    }

    @Override
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }
}