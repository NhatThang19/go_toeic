package com.vn.go_toeic.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
public class ResetPasswordReq {

    @NotBlank(message = "Token không hợp lệ.")
    private String token;

    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    private String password;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu.")
    private String confirmPassword;
}