package com.vn.go_toeic.service.cronjob;

import com.vn.go_toeic.model.User;
import com.vn.go_toeic.repository.UserRepository;
import com.vn.go_toeic.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void removeUnverifiedUsers() {
        log.info("Scheduler: Bắt đầu quét dọn các tài khoản chưa kích hoạt...");

        LocalDateTime cutOffTime = LocalDateTime.now().minusHours(48);

        List<User> unverifiedUsers = userRepository.findByVerifiedFalseAndCreatedAtBefore(cutOffTime);

        if (unverifiedUsers.isEmpty()) {
            log.info("Scheduler: Không tìm thấy tài khoản rác nào cần xóa.");
            return;
        }

        log.info("Scheduler: Tìm thấy {} tài khoản rác. Tiến hành xóa...", unverifiedUsers.size());

        int count = 0;
        for (User user : unverifiedUsers) {
            try {
                tokenRepository.findByUser(user).ifPresent(token -> {
                    tokenRepository.delete(token);
                    log.debug("Scheduler: Đã xóa token của user {}", user.getEmail());
                });

                String email = user.getEmail();
                userRepository.delete(user);

                count++;
                log.debug("Scheduler: Đã xóa user rác: {}", email);

            } catch (Exception e) {
                log.error("Scheduler: Lỗi khi xóa user id={}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Scheduler: Hoàn tất dọn dẹp. Tổng số tài khoản đã xóa: {}", count);
    }
}
