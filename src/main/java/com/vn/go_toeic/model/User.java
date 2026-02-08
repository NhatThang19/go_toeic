package com.vn.go_toeic.model;

import com.vn.go_toeic.util.abs.AuditEntity;
import com.vn.go_toeic.util.enums.GenderEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    private String avatarUrl;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Column(nullable = false)
    @Builder.Default
    private boolean locked = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    private LocalDate dateOfBirth;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialAccount> socialAccounts = new ArrayList<>();
}
