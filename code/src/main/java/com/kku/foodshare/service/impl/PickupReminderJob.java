package com.kku.foodshare.service.impl;

import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.pickup-reminder.scheduled", havingValue = "true", matchIfMissing = true)
public class PickupReminderJob {
  private final PickupReminderService reminders;
  public PickupReminderJob(PickupReminderService reminders) { this.reminders = reminders; }

  @Scheduled(fixedDelay = 60000, initialDelay = 15000)
  public void remindBeforePickupEnds() {
    try { reminders.remindDue(null); }
    catch (RuntimeException failure) {
      LoggerFactory.getLogger(getClass()).warn("Unable to check pickup reminders; will retry on the next interval", failure);
    }
  }
}
