package com.kku.foodshare.mapper;

import com.kku.foodshare.domain.entity.FoodCategory;
import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.dto.response.MapFoodPostResponse;
import org.springframework.stereotype.Component;

@Component
public class FoodPostMapper implements FoodPostMapping {

  public MapFoodPostResponse toMapResponse(FoodPost post) {

    return new MapFoodPostResponse(
        post.getId(),
        post.getTitle(),
        post.getDescription(),
        post.getCategory().name(),
        post.getQuantity(),
        post.getUnit(),
        post.getPickupLocationName(),
        post.getLatitude(),
        post.getLongitude(),
        post.getAvailableFrom(),
        post.getAvailableUntil(),
        post.getStatus().name(),
        resolveCategoryIcon(post.getCategory()));
  }

  private String resolveCategoryIcon(FoodCategory category) {

    return switch (category) {
      case FOOD -> "🍱";
      case DRINK -> "🥤";
      case SNACK -> "🍰";
    };
  }
}
