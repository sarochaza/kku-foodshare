package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.UserProfileResponse;

public interface UserProfileService {

    UserProfileResponse getProfile(
            String email,
            String providerLabel
    );
}