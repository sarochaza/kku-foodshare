package com.kku.foodshare.config;

import com.kku.foodshare.security.GoogleOAuth2SuccessHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // เอาไว้เข้ารหัส Password ก่อนเก็บลง DB
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler)
            throws Exception {

        http

            // =========================
            // AUTHORIZE
            // =========================
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/login",
                    "/register",

                    "/css/**",
                    "/js/**",
                    "/images/**",

                    // Google OAuth2
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()

                .anyRequest().authenticated()
            )


            // =========================
            // EMAIL / PASSWORD LOGIN
            // =========================
            .formLogin(form -> form

                .loginPage("/login")

                // ใช้ email แทน username
                .usernameParameter("email")

                // Login สำเร็จกลับหน้า Home
                .defaultSuccessUrl("/", true)

                .permitAll()
            )


            // =========================
            // GOOGLE LOGIN
            // =========================
            .oauth2Login(oauth -> oauth

                // ใช้หน้า Login ของเราเอง
                .loginPage("/login")

                // Google Login สำเร็จ
                .successHandler(
                    googleOAuth2SuccessHandler
                )
            )


            // =========================
            // LOGOUT
            // =========================
            .logout(logout -> logout
                .permitAll()
            );

        return http.build();
    }
}