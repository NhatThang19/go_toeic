package com.vn.go_toeic.repository;

import com.vn.go_toeic.model.Role;
import com.vn.go_toeic.util.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleEnum name);
}
