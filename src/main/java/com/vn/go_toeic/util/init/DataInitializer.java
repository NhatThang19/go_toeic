package com.vn.go_toeic.util.init;

import com.vn.go_toeic.util.init.data.RoleDataInitializer;
import com.vn.go_toeic.util.init.data.UserDataInitializer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DataInitializer implements CommandLineRunner {
    private final RoleDataInitializer roleDataInitializer;
    private  final UserDataInitializer userDataInitializer;

    @Override
    public void run(String @NonNull ... args) throws Exception {
        log.info("Bắt đầu khởi tạo dữ liệu mẫu...");

        roleDataInitializer.loadData();
        userDataInitializer.loadData();

        log.info("Hoàn tất khởi tạo dữ liệu mẫu.");
    }
}
