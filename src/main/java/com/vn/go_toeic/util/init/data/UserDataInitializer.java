package com.vn.go_toeic.util.init.data;

import com.vn.go_toeic.model.Role;
import com.vn.go_toeic.model.User;
import com.vn.go_toeic.repository.RoleRepository;
import com.vn.go_toeic.repository.UserRepository;
import com.vn.go_toeic.util.enums.RoleEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserDataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.user.default-avatar-url:https://via.placeholder.com/150}")
    private String DEFAULT_AVATAR;

    public void loadData() {
        if (userRepository.count() > 0) {
            log.info("Dữ liệu User đã tồn tại. Bỏ qua khởi tạo.");
            return;
        }

        log.info("Đang khởi tạo dữ liệu mẫu cho Users...");

        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));

        Role userRole = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));

        User admin = User.builder()
                .email("admin@gotoeic.com")
                .password(passwordEncoder.encode("123456"))
                .fullName("System Administrator")
                .verified(true)
                .locked(false)
                .avatarUrl(DEFAULT_AVATAR)
                .roles(Set.of(adminRole))
                .build();

        User normalUser = User.builder()
                .email("student@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .fullName("Nguyen Van Hoc")
                .verified(true)
                .locked(false)
                .avatarUrl(DEFAULT_AVATAR)
                .roles(Set.of(userRole))
                .build();

        userRepository.saveAll(List.of(admin, normalUser));

        log.info("Đã khởi tạo thành công {} Users.", userRepository.count());
    }
}