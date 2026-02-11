package com.vn.go_toeic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

    private static final int verifyTokenExpire = 15;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private LocalDateTime expiryDate;

    private LocalDateTime lastSendDate;

    public VerificationToken(User user) {
        this.user = user;
        this.updateToken();
    }

    public void updateToken() {
        this.expiryDate = LocalDateTime.now().plusMinutes(verifyTokenExpire);
        this.token = UUID.randomUUID().toString();
        this.lastSendDate = LocalDateTime.now();
    }
}