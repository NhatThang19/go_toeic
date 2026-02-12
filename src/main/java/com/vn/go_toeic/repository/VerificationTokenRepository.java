package com.vn.go_toeic.repository;

import com.vn.go_toeic.model.VerificationToken;
import com.vn.go_toeic.model.User;
import com.vn.go_toeic.util.enums.TokenTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, TokenTypeEnum type);

    Optional<VerificationToken> findByUser(User user);
}