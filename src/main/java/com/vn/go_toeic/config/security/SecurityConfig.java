package com.vn.go_toeic.config.security;

import com.vn.go_toeic.config.CustomAuthenticationFailureHandler;
import com.vn.go_toeic.config.CustomAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/payment/**")
                )

                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/webjars/**", "/admin/assets/**", "/css/**", "/svg/**", "/js/**", "/images/**").permitAll()

                        .requestMatchers("/", "/dang-nhap", "/dang-ky/**", "/kiem-tra-email", "/kich-hoat-tai-khoan",
                                "/quen-mat-khau/**", "/dat-lai-mat-khau").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/supporter/**").hasRole("SUPPORTER")

                        .requestMatchers(
                                "/api/**", "/profile", "/checkout",
                                "/my-courses/**", "/my-tests/**", "/my-progress/**",
                                "/study/**", "/practice/**"
                        ).authenticated()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/supporter/**").hasRole("SUPPORTER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/dang-nhap")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/dang-xuat")
                        .logoutSuccessUrl("/dang-nhap?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll())
                .rememberMe(remember -> remember
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(2592000)
                        .userDetailsService(userDetailsService))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .expiredUrl("/dang-nhap?expired"));

        return http.build();
    }
}
