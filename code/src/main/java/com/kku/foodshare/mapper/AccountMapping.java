package com.kku.foodshare.mapper;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.dto.response.AccountPageResponse;
import java.util.List;

public interface AccountMapping {
  AccountPageResponse toResponse(User user, String providerLabel, List<FoodPost> posts,
      long availablePosts, long sharedPosts);
}
