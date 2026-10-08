package com.kku.foodshare.service.discovery;

import com.kku.foodshare.domain.entity.FoodPost;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Component;

@Component
public class LatestFoodStrategy implements FoodDiscoveryStrategy {
  public String key() {
    return "latest";
  }

  public Order order(CriteriaBuilder cb, Root<FoodPost> p, Double lat, Double lng) {
    return cb.desc(p.get("createdAt"));
  }
}
