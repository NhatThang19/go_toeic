package com.vn.go_toeic.util.init.data;

import com.vn.go_toeic.model.Role;
import com.vn.go_toeic.repository.RoleRepository;
import com.vn.go_toeic.util.enums.RoleEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleDataInitializer {

    private final RoleRepository roleRepository;

    public void loadData() {
        if (roleRepository.count() > 0) {
            log.info("Role đã được khởi tạo.");
            return;
        }

        log.info("Khởi tạo role...");
        roleRepository.save(new Role(RoleEnum.ADMIN));
        roleRepository.save(new Role(RoleEnum.SUPPORTER));
        roleRepository.save(new Role(RoleEnum.USER));
        log.info("Hoàn tất khởi tạo role.");
    }
}
