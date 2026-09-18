package com.kku.foodshare.mapper;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.response.UserProfileResponse;

import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(
            User user,
            String providerLabel) {

        return new UserProfileResponse(
                resolveDisplayName(user),
                user.getEmail(),
                providerLabel
        );
    }

    private String resolveDisplayName(User user) {

        String displayName =
                user.getDisplayName();

        if (displayName != null
                && !displayName.isBlank()) {

            return displayName.trim();
        }

        String email =
                user.getEmail();

        int atIndex =
                email.indexOf("@");

        if (atIndex > 0) {
            return email.substring(
                    0,
                    atIndex
            );
        }

        return email;
    }
}