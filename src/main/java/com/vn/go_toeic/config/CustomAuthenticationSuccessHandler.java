package com.vn.go_toeic.config;

import com.vn.go_toeic.util.enums.RoleEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        var authorities = authentication.getAuthorities();
        String targetUrl = "/";

        if (authorities.stream().anyMatch(a -> Objects.equals(a.getAuthority(), RoleEnum.ADMIN.toString()))) {
            targetUrl = "/admin";
        } else if (authorities.stream().anyMatch(a -> Objects.equals(a.getAuthority(), RoleEnum.USER.toString()))) {
            targetUrl = "/";
        }

        response.sendRedirect(targetUrl);
    }
}
