package com.vn.go_toeic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {
    @GetMapping("/dang-nhap")
    public String getLoginPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            return "redirect:/";
        }
        return "auth/login";
    }

    @GetMapping("dang-ky")
    public String getRegisterPage() {
        return "auth/register";
    }
}
