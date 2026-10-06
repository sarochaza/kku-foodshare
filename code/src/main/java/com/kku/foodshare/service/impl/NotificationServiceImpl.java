package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.Notification;
import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.NotificationRepository;
import com.kku.foodshare.service.*;
import com.kku.foodshare.service.event.ActivityNotice;
import java.time.*;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
  private final NotificationRepository repo;
  private final MemberService members;
  private final Clock clock;

  public NotificationServiceImpl(NotificationRepository repo, MemberService members, Clock clock) {
    this.repo = repo;
    this.members = members;
    this.clock = clock;
  }

  @EventListener
  public void notify(ActivityNotice e) {
    Notification n = new Notification();
    n.user = e.user();
    n.title = e.title();
    n.message = e.message();
    n.href = e.href();
    n.createdAt = LocalDateTime.now(clock);
    repo.save(n);
  }

  @Transactional(readOnly = true)
  public PageView<View> list(String email, int page) {
    Long uid = members.require(email).getId();
    return PageView.of(
        repo.findByUserIdOrderByCreatedAtDesc(uid, PageRequest.of(Math.max(0, page), 20))
            .map(n -> new View(n.id, n.title, n.message, n.href, n.createdAt, n.readAt != null)));
  }

  public void read(String email, long id) {
    Long uid = members.require(email).getId();
    Notification n = repo.findById(id).orElseThrow(Problem::missing);
    if (!n.user.getId().equals(uid)) throw Problem.forbidden();
    n.readAt = LocalDateTime.now(clock);
  }

  @Transactional(readOnly = true)
  public long unread(String email) {
    return repo.countByUserIdAndReadAtIsNull(members.require(email).getId());
  }
}
