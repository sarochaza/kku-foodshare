package com.kku.foodshare.service.discovery;

import com.kku.foodshare.domain.entity.FoodPost;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Component;

@Component
public class ExpiryFoodStrategy implements FoodDiscoveryStrategy {
  public String key() {
    return "expiry";
  }

  public Order order(CriteriaBuilder cb, Root<FoodPost> p, Double lat, Double lng) {
    return cb.asc(p.get("availableUntil"));
  }
}
