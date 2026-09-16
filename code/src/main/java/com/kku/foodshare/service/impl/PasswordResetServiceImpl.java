package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.PasswordResetToken;
import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.PasswordResetTokenRepository;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.EmailService;
import com.kku.foodshare.service.PasswordResetService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetServiceImpl
        implements PasswordResetService {

    // Token ใช้งานได้ 15 นาที
    private static final Duration TOKEN_LIFETIME =
            Duration.ofMinutes(15);

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final UserRepository userRepository;

    private final PasswordResetTokenRepository
            tokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final Clock clock;

    private final String baseUrl;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            Clock clock,
            @Value(
                "${app.base-url:http://localhost:8080}"
            )
            String baseUrl) {

        this.userRepository = userRepository;

        this.tokenRepository = tokenRepository;

        this.passwordEncoder = passwordEncoder;

        this.emailService = emailService;

        this.clock = clock;

        // ลบ / ด้านท้าย URL ถ้ามี
        this.baseUrl =
                baseUrl.replaceAll("/+$", "");
    }

    @Override
    @Transactional
    public void requestReset(String email) {

        String normalizedEmail =
                email.trim()
                     .toLowerCase(Locale.ROOT);

        /*
         * ถ้าไม่พบอีเมล จะไม่แจ้ง Error
         * เพื่อไม่เปิดเผยว่ามีบัญชีนี้อยู่หรือไม่
         */
        userRepository
                .findByEmailIgnoreCase(
                        normalizedEmail
                )
                .ifPresent(
                        this::createAndSendToken
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(
            String rawToken) {

        if (
            rawToken == null ||
            rawToken.isBlank()
        ) {
            return false;
        }

        Instant now = clock.instant();

        return tokenRepository
                .findByTokenHash(
                        hash(rawToken)
                )
                .filter(
                        token ->
                                token.getUsedAt()
                                == null
                )
                .filter(
                        token ->
                                token.getExpiresAt()
                                     .isAfter(now)
                )
                .isPresent();
    }

    @Override
    @Transactional
    public void resetPassword(
            String rawToken,
            String newPassword) {

        Instant now = clock.instant();

        PasswordResetToken token =
                tokenRepository
                    .findByTokenHash(
                            hash(rawToken)
                    )
                    .filter(
                            current ->
                                current.getUsedAt()
                                == null
                    )
                    .filter(
                            current ->
                                current.getExpiresAt()
                                       .isAfter(now)
                    )
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Reset token is invalid or expired"
                            )
                    );

        User user = token.getUser();

        // เข้ารหัสรหัสผ่านใหม่ด้วย BCrypt
        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        // บันทึกว่า Token ถูกใช้แล้ว
        token.setUsedAt(now);

        userRepository.save(user);

        tokenRepository.save(token);
    }

    private void createAndSendToken(
            User user) {

        /*
         * ลบ Token เก่าของผู้ใช้
         * ทำให้ลิงก์ก่อนหน้าใช้ไม่ได้
         */
        tokenRepository.deleteAllByUser(
                user
        );

        byte[] randomBytes =
                new byte[32];

        SECURE_RANDOM.nextBytes(
                randomBytes
        );

        String rawToken =
                Base64.getUrlEncoder()
                      .withoutPadding()
                      .encodeToString(
                              randomBytes
                      );

        Instant now = clock.instant();

        PasswordResetToken token =
                new PasswordResetToken();

        /*
         * เก็บเฉพาะ Hash ลงฐานข้อมูล
         * ไม่เก็บ Token ตัวจริง
         */
        token.setTokenHash(
                hash(rawToken)
        );

        token.setUser(user);

        token.setCreatedAt(now);

        token.setExpiresAt(
                now.plus(TOKEN_LIFETIME)
        );

        tokenRepository.save(token);

        String resetUrl =
                baseUrl +
                "/reset-password?token=" +
                rawToken;

        emailService
                .sendPasswordResetEmail(
                        user.getEmail(),
                        resetUrl
                );
    }

    private String hash(
            String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hashedBytes =
                    digest.digest(
                        value.getBytes(
                            StandardCharsets.UTF_8
                        )
                    );

            return HexFormat.of()
                    .formatHex(hashedBytes);

        } catch (
            NoSuchAlgorithmException exception
        ) {

            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }
}