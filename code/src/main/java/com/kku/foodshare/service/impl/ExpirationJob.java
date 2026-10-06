package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.FoodPostStatus;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.service.ReservationService;
import java.time.*;
import java.util.List;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ExpirationJob {
  private final FoodPostRepository posts;
  private final ReservationService reservations;
  private final Clock clock;

  public ExpirationJob(FoodPostRepository posts, ReservationService reservations, Clock clock) {
    this.posts = posts;
    this.reservations = reservations;
    this.clock = clock;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 60000)
  public void run() {
    for (var p :
        posts.findByAvailableUntilBeforeAndStatusIn(
            LocalDateTime.now(clock), List.of(FoodPostStatus.AVAILABLE, FoodPostStatus.LOW_STOCK)))
      try {
        reservations.expire(p.getId());
      } catch (RuntimeException e) {
        org.slf4j.LoggerFactory.getLogger(getClass())
            .error("Expiration failed for post {}", p.getId(), e);
      }
  }
}
