package com.vn.go_toeic.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String redirectUrl = "/login?error";

        if (exception instanceof DisabledException) {
            redirectUrl = "/login?unverified";
        } else if (exception instanceof LockedException) {
            redirectUrl = "/login?locked";
        } else if (exception instanceof BadCredentialsException) {
            redirectUrl = "/login?error";
        }

        String fullUrl = request.getContextPath() + redirectUrl;
        response.sendRedirect(fullUrl);
    }
}
