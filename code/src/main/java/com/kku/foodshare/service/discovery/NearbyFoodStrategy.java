package com.kku.foodshare.service.discovery;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.exception.Problem;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Component;

@Component
public class NearbyFoodStrategy implements FoodDiscoveryStrategy {
  public String key() {
    return "nearby";
  }

  public Order order(CriteriaBuilder cb, Root<FoodPost> p, Double lat, Double lng) {
    if (lat == null || lng == null)
      throw new Problem(400, "กรุณาเปิดตำแหน่งเพื่อค้นหาอาหารใกล้คุณ");
    Expression<Double> latR = cb.function("radians", Double.class, p.get("latitude"));
    Expression<Double> lngR = cb.function("radians", Double.class, p.get("longitude"));
    Expression<Double> cosDistance =
        cb.sum(
            cb.prod(cb.function("sin", Double.class, latR), Math.sin(Math.toRadians(lat))),
            cb.prod(
                cb.prod(cb.function("cos", Double.class, latR), Math.cos(Math.toRadians(lat))),
                cb.function("cos", Double.class, cb.diff(lngR, Math.toRadians(lng)))));
    return cb.desc(cosDistance);
  }
}
