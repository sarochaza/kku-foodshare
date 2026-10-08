package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.repository.NotificationRepository;
import com.kku.foodshare.repository.ReservationRepository;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class PickupReminderService {
  private static final Set<FoodPostStatus> OPEN = Set.of(FoodPostStatus.AVAILABLE, FoodPostStatus.LOW_STOCK);
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
  private final ReservationRepository reservations;
  private final NotificationRepository notifications;
  private final Clock clock;

  public PickupReminderService(ReservationRepository reservations, NotificationRepository notifications, Clock clock) {
    this.reservations = reservations; this.notifications = notifications; this.clock = clock;
  }

  // Inbox reads may run in a read-only transaction; reminders need their own write transaction.
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public int remindDue(Long memberId) {
    LocalDateTime now = LocalDateTime.now(clock);
    var ids = reservations.findDuePickupReminderIds(now, now.plusMinutes(30), OPEN, memberId, PageRequest.of(0, 100));
    int created = 0;
    for (long id : ids) {
      // Lock only this reservation, never the food stock row. Scheduler and inbox use the same lock.
      Reservation r = reservations.lockForPickupReminder(id).orElse(null);
      if (r == null || r.status != ReservationStatus.RESERVED
          || !Boolean.TRUE.equals(r.member.getActive()) || !Boolean.TRUE.equals(r.post.getOwner().getActive())
          || !OPEN.contains(r.post.getStatus()) || memberId != null && !memberId.equals(r.member.getId())) continue;
      LocalDateTime current = LocalDateTime.now(clock), deadline = r.post.getAvailableUntil();
      if (!deadline.isAfter(current) || deadline.isAfter(current.plusMinutes(30))) continue;
      String key = "pickup-deadline:" + r.id;
      if (notifications.existsByUserIdAndDedupeKey(r.member.getId(), key)) continue;
      long minutes = Math.max(1, (Duration.between(current, deadline).toSeconds() + 59) / 60);
      Notification n = new Notification();
      n.user = r.member; n.actorUserId = r.post.getOwner().getId(); n.actorName = r.post.getOwner().getDisplayName();
      n.title = "ใกล้หมดเวลารับอาหารที่จอง";
      n.message = "รับภายใน " + deadline.format(TIME) + " น. · เหลือประมาณ " + minutes + " นาที · “"
          + r.post.getTitle() + "” · " + r.quantity + " " + r.post.getUnit()
          + " · " + r.post.getPickupLocationName();
      if (n.message.length() > 500) n.message = n.message.substring(0, 499) + "…";
      n.href = "/reservations#reservation-" + r.id;
      n.dedupeKey = key; n.createdAt = current;
      notifications.save(n); created++;
    }
    return created;
  }
}
