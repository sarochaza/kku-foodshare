package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.dto.response.PostView;

public interface PostViewService {
  PostView map(FoodPost post, String email, Double latitude, Double longitude);
}
