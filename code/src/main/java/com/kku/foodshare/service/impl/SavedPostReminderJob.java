package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.FoodPostStatus;
import com.kku.foodshare.repository.SavedPostRepository;
import com.kku.foodshare.service.NotificationService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SavedPostReminderJob {
  private final SavedPostRepository saved;
  private final NotificationService notifications;
  private final Clock clock;

  public SavedPostReminderJob(SavedPostRepository saved, NotificationService notifications, Clock clock) {
    this.saved = saved; this.notifications = notifications; this.clock = clock;
  }

  @Scheduled(fixedDelay = 300000, initialDelay = 120000)
  @Transactional
  public void remindBeforeDeadline() {
    LocalDateTime now = LocalDateTime.now(clock);
    for (var entry : saved.findNearDeadline(now, now.plusMinutes(30), List.of(FoodPostStatus.AVAILABLE, FoodPostStatus.LOW_STOCK))) {
      if (entry.getUser().getActive()) notifications.notifySavedPostDeadline(entry.getUser(), entry.getPost());
    }
  }
}
