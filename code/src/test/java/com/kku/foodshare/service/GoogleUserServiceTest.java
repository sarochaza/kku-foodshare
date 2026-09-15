package com.kku.foodshare.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.impl.GoogleUserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleUserServiceTest {

    private UserRepository userRepository;
    private GoogleUserService googleUserService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);

    googleUserService =
            new GoogleUserServiceImpl(
                    userRepository,
                    passwordEncoder
            );
}

    @Test
    void shouldReturnExistingUserWhenGoogleEmailAlreadyExists() {

        User existingUser = new User();
        existingUser.setEmail("test@gmail.com");
        existingUser.setDisplayName("Test User");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        User result = googleUserService.findOrCreateGoogleUser(
                "test@gmail.com",
                "Test User"
        );

        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("Test User", result.getDisplayName());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldCreateNewUserWhenGoogleEmailDoesNotExist() {

        when(userRepository.findByEmail("new@gmail.com"))
                .thenReturn(Optional.empty());

        User savedUser = new User();
        savedUser.setEmail("new@gmail.com");
        savedUser.setDisplayName("New User");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = googleUserService.findOrCreateGoogleUser(
                "new@gmail.com",
                "New User"
        );

        assertEquals("new@gmail.com", result.getEmail());
        assertEquals("New User", result.getDisplayName());

        verify(userRepository, times(1))
                .save(any(User.class));
    }
}