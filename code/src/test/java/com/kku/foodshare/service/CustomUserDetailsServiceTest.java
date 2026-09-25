package com.kku.foodshare.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.core.userdetails.UserDetails;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.domain.enums.UserRole;
import com.kku.foodshare.repository.UserRepository;

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
    // ทดสอบว่า Spring Security แปลงสิทธิ์ ADMIN เป็น ROLE_ADMIN
@Test
void shouldLoadAdminAuthorityFromUserRole() {

    UserRepository userRepository =
            mock(UserRepository.class);

    User admin = new User();

    admin.setEmail("admin@kku.ac.th");
    admin.setPassword("$2a$10$hashedpassword");
    admin.setDisplayName("FoodShare Admin");
    admin.setActive(true);
    admin.setRole(UserRole.ADMIN);

    when(
            userRepository.findByEmail(
                    "admin@kku.ac.th"
            )
    ).thenReturn(
            Optional.of(admin)
    );

    CustomUserDetailsService service =
            new CustomUserDetailsService(
                    userRepository
            );

    UserDetails result =
            service.loadUserByUsername(
                    "admin@kku.ac.th"
            );

    assertTrue(
            result.getAuthorities()
                    .stream()
                    .anyMatch(
                            authority ->
                                    authority.getAuthority()
                                            .equals(
                                                    "ROLE_ADMIN"
                                            )
                    )
    );
}
}