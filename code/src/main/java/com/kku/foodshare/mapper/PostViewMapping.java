package com.kku.foodshare.mapper;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.dto.response.*;

public interface PostViewMapping {
  PostView map(FoodPost post, String email, Double latitude, Double longitude, PostViewContext context);
}
