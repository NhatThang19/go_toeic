package com.vn.go_toeic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.auth.verify-token-expire}")
    private int verifyTokenExpire;

    @Value("${app.auth.reset-password-token-expire}")
    private int resetPasswordTokenExpire;
}
