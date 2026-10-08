package com.kku.foodshare.service.discovery;

import com.kku.foodshare.domain.entity.FoodPost;
import jakarta.persistence.criteria.*;

public interface FoodDiscoveryStrategy {
  String key();

  /**
   * Coordinates are optional for latest/expiry; nearby requires both latitude and longitude.
   * The catalog validates bounds; missing nearby coordinates are reported as HTTP 400.
   */
  Order order(CriteriaBuilder cb, Root<FoodPost> p, Double lat, Double lng);
}
