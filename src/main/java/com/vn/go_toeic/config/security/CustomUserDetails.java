package com.vn.go_toeic.config.security;

import com.vn.go_toeic.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Integer id;
    private final String email;
    private final String fullName;
    private final String password;
    private final String avatarUrl;
    private final boolean locked;
    private final boolean verified;
    private final LocalDateTime createdAt;
    private final Collection<? extends GrantedAuthority> authorities;

    @Setter
    private Map<String, Object> attributes;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.password = user.getPassword();
        this.avatarUrl = user.getAvatarUrl();
        this.locked = user.isLocked();
        this.createdAt = user.getCreatedAt();
        this.verified = user.getVerified();

        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());
    }

    // Các phương thức của UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.verified;
    }

    // Các phương thức của OAuth2User

    @Override
    public String getName() {
        return this.email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

}
