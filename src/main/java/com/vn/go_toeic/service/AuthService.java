package com.vn.go_toeic.service;

import com.vn.go_toeic.dto.req.EmailReq;
import com.vn.go_toeic.dto.req.UserRegisterReq;
import com.vn.go_toeic.exception.EmailAlreadyExistsException;
import com.vn.go_toeic.model.Role;
import com.vn.go_toeic.model.User;
import com.vn.go_toeic.model.VerificationToken;
import com.vn.go_toeic.repository.RoleRepository;
import com.vn.go_toeic.repository.VerificationTokenRepository;
import com.vn.go_toeic.util.enums.RoleEnum;
import com.vn.go_toeic.util.enums.TokenTypeEnum;
import com.vn.go_toeic.util.mapper.UserMapper;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository tokenRepository;

    @Value("${app.domain}")
    private String domain;

    @Value("${app.auth.verify-token-expire}")
    private int verifyTokenExpire;

    @Value("${app.user.default-avatar-url}")
    private String avatarDefault;

    @Transactional
    public void registerAccount(UserRegisterReq req) throws MessagingException {
        log.info("Service: Bắt đầu quy trình đăng ký tài khoản cho email: {}", req.getEmail());

        Optional<User> existingUserOpt = userService.findByEmail(req.getEmail());
        User user;

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getVerified()) {
                throw new EmailAlreadyExistsException("Địa chỉ email này đã được sử dụng và kích hoạt.");
            }

            user = existingUser;
            user.setFullName(req.getFullName());
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            userService.save(user);

            Optional<VerificationToken> oldToken = tokenRepository.findByUser(user);
            if (oldToken.isPresent()) {
                log.debug("Service: Xóa token xác thực cũ id={} của user {}", oldToken.get().getId(), user.getEmail());
                tokenRepository.delete(oldToken.get());
                tokenRepository.flush();
            }
        } else {
            log.info("Service: Tạo mới User entity cho email: {}", req.getEmail());

            user = userMapper.toEntity(req);
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            user.setAvatarUrl(avatarDefault);
            user.setVerified(false);

            Role userRole = roleRepository.findByName(RoleEnum.valueOf(RoleEnum.USER.name()))
                    .orElseThrow(() -> {
                        log.error("Service: Lỗi cấu hình nghiêm trọng - Không tìm thấy ROLE_USER trong Database");
                        return new RuntimeException("Không tìm thấy quyền USER trong hệ thống");
                    });
            user.setRoles(Set.of(userRole));

            userService.save(user);
        }

        tokenRepository.findByUser(user);

        VerificationToken verificationToken = new VerificationToken(user);
        verificationToken.setType(TokenTypeEnum.VERIFICATION);
        tokenRepository.save(verificationToken);
        log.debug("Service: Đã lưu VerificationToken mới cho user {}", user.getEmail());

        sendVerificationEmail(user, verificationToken);
    }

    @Transactional
    public void verifyAccount(String token) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(token, TokenTypeEnum.VERIFICATION)
                .orElseThrow(() -> {
                    log.warn("Service: Token xác thực không tồn tại hoặc sai loại: {}", token);
                    return new RuntimeException("Token kích hoạt không hợp lệ.");
                });

        User user = verificationToken.getUser();

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            throw new RuntimeException("Link kích hoạt đã hết hạn. Vui lòng đăng ký lại hoặc yêu cầu gửi lại mail.");
        }

        if (!user.getVerified()) {
            user.setVerified(true);
            userService.save(user);
            log.info("Service: Đã kích hoạt user {}", user.getEmail());
        }

        tokenRepository.delete(verificationToken);
    }

    @Transactional
    public void resendVerificationToken(String email) throws MessagingException {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại."));

        if (user.getVerified()) {
            throw new RuntimeException("Tài khoản đã được kích hoạt.");
        }

        generateAndSendToken(user, TokenTypeEnum.VERIFICATION);
    }

    @Transactional
    public void processForgotPassword(String email) throws MessagingException {
        log.info("Service: Yêu cầu quên mật khẩu cho email: {}", email);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));

        generateAndSendToken(user, TokenTypeEnum.PASSWORD_RESET);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(token, TokenTypeEnum.PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Token đặt lại mật khẩu không hợp lệ."));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link đặt lại mật khẩu đã hết hạn.");
        }

        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        log.info("Service: Đổi mật khẩu thành công cho user {}", user.getEmail());

        tokenRepository.delete(verificationToken);
    }

    private void generateAndSendToken(User user, TokenTypeEnum type) throws MessagingException {
        VerificationToken token = tokenRepository.findByUser(user)
                .orElse(new VerificationToken(user));

        token.setType(type);
        token.updateToken();
        tokenRepository.save(token);

        if (type == TokenTypeEnum.VERIFICATION) {
            sendVerificationEmail(user, token);
        } else {
            sendResetPasswordEmail(user, token);
        }
    }

    private void sendVerificationEmail(User user, VerificationToken token) throws MessagingException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", user.getFullName());
        variables.put("link", domain + "/kich-hoat-tai-khoan?token=" + token.getToken());
        variables.put("verifyTokenExpire", verifyTokenExpire);

        EmailReq emailReq = EmailReq.builder()
                .to(user.getEmail())
                .subject("Xác thực tài khoản GoToeic")
                .templateName("email/verification-email")
                .variables(variables)
                .build();

        emailService.sendEmail(emailReq);
    }

    private void sendResetPasswordEmail(User user, VerificationToken token) throws MessagingException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", user.getFullName());

        variables.put("link", domain + "/dat-lai-mat-khau?token=" + token.getToken());
        variables.put("verifyTokenExpire", verifyTokenExpire);

        EmailReq emailReq = EmailReq.builder()
                .to(user.getEmail())
                .subject("Yêu cầu đặt lại mật khẩu - GoToeic")
                .templateName("email/verification-reset-password-email")
                .variables(variables)
                .build();

        log.info("Service: Gửi mail reset pass tới {}", user.getEmail());
        emailService.sendEmail(emailReq);
    }
}