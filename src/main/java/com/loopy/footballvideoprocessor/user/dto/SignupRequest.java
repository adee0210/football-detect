package com.loopy.footballvideoprocessor.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @Schema(description = "Tên đăng nhập của người dùng", example = "test01")
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    private String username;

    @Schema(description = "Email của người dùng", example = "test01@example.com")
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Schema(description = "Mật khẩu của người dùng", example = "test01")
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 40, message = "Mật khẩu phải từ 6 đến 40 ký tự")
    private String password;

    @Schema(description = "Tên hiển thị của người dùng", example = "Test User")
    @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
    private String name;
}
