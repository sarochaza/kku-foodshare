package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Test
    void shouldLoadUserByEmail() {

        // Arrange
        UserRepository userRepository = mock(UserRepository.class);

        User user = new User();
        user.setEmail("test@kku.ac.th");
        user.setPassword("$2a$10$hashedpassword");
        user.setDisplayName("Test User");
        user.setActive(true);

        when(userRepository.findByEmail("test@kku.ac.th"))
                .thenReturn(Optional.of(user));

        CustomUserDetailsService service =
                new CustomUserDetailsService(userRepository);

        // Act
        UserDetails result =
                service.loadUserByUsername("test@kku.ac.th");

        // Assert
        assertEquals("test@kku.ac.th", result.getUsername());
        assertEquals("$2a$10$hashedpassword", result.getPassword());
    }
}