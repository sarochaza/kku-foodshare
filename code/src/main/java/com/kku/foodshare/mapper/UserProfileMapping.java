package com.kku.foodshare.mapper;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.response.UserProfileResponse;

public interface UserProfileMapping {
  UserProfileResponse toResponse(User user, String providerLabel);
}
