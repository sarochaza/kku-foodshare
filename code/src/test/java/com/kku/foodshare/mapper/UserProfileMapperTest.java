package com.kku.foodshare.mapper;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.response.UserProfileResponse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserProfileMapperTest {

    @Test
    void shouldUseRegisteredDisplayNameForEmailAccount() {

        User user = new User();

        user.setDisplayName("ชมพู่ เสาทอง");
        user.setEmail("sarocha@kku.ac.th");

        UserProfileMapper mapper =
                new UserProfileMapper();

        UserProfileResponse response =
                mapper.toResponse(
                        user,
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
    }
    @Test
void shouldUseEmailPrefixWhenDisplayNameIsBlank() {

    User user = new User();

    user.setDisplayName("   ");
    user.setEmail("sarocha2548@kku.ac.th");

    UserProfileMapper mapper =
            new UserProfileMapper();

    UserProfileResponse response =
            mapper.toResponse(
                    user,
                    "บัญชีอีเมล"
            );

    assertEquals(
            "sarocha2548",
            response.getDisplayName()
    );
}
}