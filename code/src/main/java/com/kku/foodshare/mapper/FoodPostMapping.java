package com.kku.foodshare.mapper;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.dto.response.MapFoodPostResponse;

public interface FoodPostMapping {
  MapFoodPostResponse toMapResponse(FoodPost post);
}
