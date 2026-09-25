package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.response.UserProfileResponse;
import com.kku.foodshare.mapper.UserProfileMapper;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.impl.UserProfileServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldReturnProfileOfUserByEmail() {

        User user = new User();

        user.setDisplayName("ชมพู่ เสาทอง");
        user.setEmail("sarocha@kku.ac.th");

        when(
                userRepository.findByEmailIgnoreCase(
                        "sarocha@kku.ac.th"
                )
        ).thenReturn(
                Optional.of(user)
        );

        UserProfileService service =
                new UserProfileServiceImpl(
                        userRepository,
                        new UserProfileMapper()
                );

        UserProfileResponse response =
                service.getProfile(
                        "sarocha@kku.ac.th",
                        "บัญชีอีเมล"
                );

        assertEquals(
                "ชมพู่ เสาทอง",
                response.getDisplayName()
        );

        assertEquals(
                "sarocha@kku.ac.th",
                response.getEmail()
        );

        assertEquals(
                "บัญชีอีเมล",
                response.getProviderLabel()
        );

        verify(userRepository)
                .findByEmailIgnoreCase(
                        "sarocha@kku.ac.th"
                );
    }

    @Test
void shouldThrowExceptionWhenUserDoesNotExist() {

    when(
            userRepository.findByEmailIgnoreCase(
                    "missing@kku.ac.th"
            )
    ).thenReturn(
            Optional.empty()
    );

    UserProfileService service =
            new UserProfileServiceImpl(
                    userRepository,
                    new UserProfileMapper()
            );

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            service.getProfile(
                                    "missing@kku.ac.th",
                                    "บัญชีอีเมล"
                            )
            );

    assertEquals(
            "User not found",
            exception.getMessage()
    );

    verify(userRepository)
            .findByEmailIgnoreCase(
                    "missing@kku.ac.th"
            );
}
}