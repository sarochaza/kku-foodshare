package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.GoogleUserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
/*GoogleUserServiceImpl เป็นตัวทำงานจริง ทำให้ Controller หรือส่วน OAuth พึ่งพา Interface 
แทน Implementation และสอดคล้องกับ Dependency Inversion */
@Service
public class GoogleUserServiceImpl
        implements GoogleUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GoogleUserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findOrCreateGoogleUser(
            String email,
            String displayName) {

        return userRepository
                .findByEmail(email)
                .orElseGet(() ->
                        createGoogleUser(
                                email,
                                displayName
                        )
                );
    }

    private User createGoogleUser(
            String email,
            String displayName) {

        User user = new User();

        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setActive(true);

        String randomPassword =
                UUID.randomUUID().toString();

        user.setPassword(
                passwordEncoder.encode(randomPassword)
        );

        return userRepository.save(user);
    }
}