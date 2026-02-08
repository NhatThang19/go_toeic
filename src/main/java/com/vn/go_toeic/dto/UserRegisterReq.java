package com.vn.go_toeic.dto;

import com.vn.go_toeic.util.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterReq {
    @NotBlank(message = "Tên người dùng không được để trống", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 3, message = "Tên người dùng phải có ít nhất 3 ký tự", groups = ValidationGroups.SizeGroup.class)
    private String fullName;

    @NotBlank(message = "Email không được để trống", groups = ValidationGroups.NotBlankGroup.class)
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự", groups = ValidationGroups.SizeGroup.class)
    private String password;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu", groups = ValidationGroups.NotBlankGroup.class)
    private String confirmPassword;
}
