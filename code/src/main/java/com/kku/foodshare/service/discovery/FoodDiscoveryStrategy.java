package com.kku.foodshare.service.discovery;

import com.kku.foodshare.domain.entity.FoodPost;
import jakarta.persistence.criteria.*;

public interface FoodDiscoveryStrategy {
  String key();

  Order order(CriteriaBuilder cb, Root<FoodPost> p, Double lat, Double lng);
}
