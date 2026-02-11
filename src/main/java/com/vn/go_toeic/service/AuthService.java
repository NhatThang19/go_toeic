package com.vn.go_toeic.service;

import com.vn.go_toeic.dto.EmailReq;
import com.vn.go_toeic.dto.UserRegisterReq;
import com.vn.go_toeic.exception.EmailAlreadyExistsException;
import com.vn.go_toeic.model.Role;
import com.vn.go_toeic.model.User;
import com.vn.go_toeic.model.VerificationToken;
import com.vn.go_toeic.repository.RoleRepository;
import com.vn.go_toeic.repository.VerificationTokenRepository;
import com.vn.go_toeic.util.enums.RoleEnum;
import com.vn.go_toeic.util.mapper.UserMapper;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

    @Value("${app.auth.resend-token-wait-time}")
    private int waitTime;

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

        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);
        log.debug("Service: Đã lưu VerificationToken mới cho user {}", user.getEmail());

        sendEmailToken(user, verificationToken);
    }

    @Transactional
    public void verifyAccount(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Service: Token không tồn tại hoặc không hợp lệ: {}", token);
                    return new RuntimeException("Token không hợp lệ hoặc không tồn tại.");
                });

        User user = verificationToken.getUser();
        log.info("Service: Đang xử lý xác thực cho email: {}", user.getEmail());

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Service: Token đã hết hạn vào lúc {} cho email {}. Tiến hành xóa token.",
                    verificationToken.getExpiryDate(), user.getEmail());

            tokenRepository.delete(verificationToken);
            throw new RuntimeException("Token đã hết hạn. Vui lòng đăng ký lại.");
        }

        if (user.getVerified()) {
            log.info("Service: User {} đã được kích hoạt trước đó.", user.getEmail());
        } else {
            user.setVerified(true);
            userService.save(user);
            log.info("Service: Đã cập nhật trạng thái verified = true cho user {}", user.getEmail());
        }

        tokenRepository.delete(verificationToken);
        log.debug("Service: Đã xóa token xác thực sau khi hoàn tất.");
    }

    @Transactional
    public void resendVerificationToken(String email) throws MessagingException {
        log.info("Service: Bắt đầu xử lý gửi lại token cho email: {}", email);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Service: Yêu cầu gửi lại thất bại - Email không tồn tại: {}", email);
                    return new RuntimeException("Email không tồn tại trong hệ thống.");
                });

        if (user.getVerified()) {
            log.warn("Service: Yêu cầu gửi lại thất bại - Tài khoản {} đã được kích hoạt trước đó.", email);
            throw new RuntimeException("Tài khoản này đã được kích hoạt.");
        }

        VerificationToken verificationToken = tokenRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("Service: Không tìm thấy token cũ, tạo mới cho user {}", email);
                    return new VerificationToken(user);
                });

        if (verificationToken.getLastSendDate() != null) {
            long secondsSinceLastSend = Duration.between(verificationToken.getLastSendDate(), LocalDateTime.now()).getSeconds();

            if (secondsSinceLastSend < waitTime) {
                long waitSeconds = waitTime - secondsSinceLastSend;
                log.warn("Service: Rate Limit - User {} gửi yêu cầu quá nhanh. Cần chờ thêm {}s", email, waitSeconds);
                throw new RuntimeException("Vui lòng đợi " + waitSeconds + " giây trước khi gửi lại.");
            }
        }

        verificationToken.updateToken();
        tokenRepository.save(verificationToken);

        log.debug("Service: Đã cập nhật token mới vào database cho user {}", email);

        sendEmailToken(user, verificationToken);
    }

    private void sendEmailToken(User user, VerificationToken verificationToken) throws MessagingException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", user.getFullName());
        variables.put("link", domain + "/kich-hoat-tai-khoan?token=" + verificationToken.getToken());
        variables.put("verifyTokenExpire", verifyTokenExpire);

        EmailReq emailReq = EmailReq.builder()
                .to(user.getEmail())
                .subject("Xác thực tài khoản GoToeic")
                .templateName("email/verification-email")
                .variables(variables)
                .build();

        log.info("Service: Đang gửi email xác thực tới {}", user.getEmail());

        emailService.sendEmail(emailReq);
    }
}
