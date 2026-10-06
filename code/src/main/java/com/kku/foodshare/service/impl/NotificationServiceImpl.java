package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.Notification;
import com.kku.foodshare.domain.entity.NotificationPreference;
import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.NotificationRepository;
import com.kku.foodshare.repository.NotificationPreferenceRepository;
import com.kku.foodshare.service.*;
import com.kku.foodshare.service.event.ActivityNotice;
import java.time.*;
import java.util.*;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
  private final NotificationRepository repo;
  private final MemberService members;
  private final NotificationPreferenceRepository preferences;
  private final Clock clock;

  public NotificationServiceImpl(NotificationRepository repo, MemberService members, NotificationPreferenceRepository preferences, Clock clock) {
    this.repo = repo;
    this.members = members;
    this.preferences = preferences;
    this.clock = clock;
  }

  @EventListener
  public void notify(ActivityNotice e) {
    save(e.user(), e.actor(), e.title(), e.message(), e.href(), null);
  }
  private void save(com.kku.foodshare.domain.entity.User user, com.kku.foodshare.domain.entity.User actor, String title, String message, String href, String key) {
    if (key != null && repo.existsByUserIdAndDedupeKey(user.getId(), key)) return;
    Notification n = new Notification();
    n.user = user; n.title = title; n.message = message; n.href = href; n.dedupeKey = key;
    n.actorUserId = actor == null ? null : actor.getId(); n.actorName = actor == null ? null : actor.getDisplayName();
    n.createdAt = LocalDateTime.now(clock);
    repo.save(n);
  }

  @Transactional(readOnly = true)
  public PageView<View> list(String email, int page) {
    Long uid = members.require(email).getId();
    return PageView.of(
        repo.findByUserIdOrderByCreatedAtDesc(uid, PageRequest.of(Math.max(0, page), 20))
            .map(n -> new View(n.id, n.title, n.message, n.href, type(n), n.actorUserId, n.actorName, n.createdAt, n.readAt != null)));
  }

  private String type(Notification n) {
    String text = (n.title + " " + n.message).toLowerCase(Locale.ROOT);
    if (text.contains("ความคิดเห็น")) return "COMMENT";
    if (text.contains("อาหารใหม่") || n.dedupeKey != null && n.dedupeKey.startsWith("interest-post:")) return "INTEREST";
    if (text.contains("จอง") || text.contains("รับอาหาร") || text.contains("reservation")) return "RESERVATION";
    return "UPDATE";
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

  @Transactional(readOnly = true)
  public Preferences preferences(String email) {
    var p = preferences.findByUserId(members.require(email).getId()).orElse(null);
    return p == null ? new Preferences(List.of(), "") : new Preferences(Arrays.stream(p.categories.split(",")).filter(s -> !s.isBlank()).toList(), p.keywords);
  }
  public Preferences updatePreferences(String email, List<String> categories, String keywords) {
    var user = members.require(email); Set<String> allowed = Set.of("FOOD", "DRINK", "SNACK");
    List<String> clean = (categories == null ? List.<String>of() : categories).stream().filter(allowed::contains).distinct().toList();
    String words = keywords == null ? "" : keywords.trim().replaceAll("\\s+", " ");
    if (words.length() > 300) throw new Problem(400, "คำค้นหายาวเกินไป");
    NotificationPreference p = preferences.findByUserId(user.getId()).orElseGet(NotificationPreference::new);
    p.user = user; p.categories = String.join(",", clean); p.keywords = words; p.updatedAt = LocalDateTime.now(clock); preferences.save(p);
    return new Preferences(clean, words);
  }
  public void notifyInterested(FoodPost post) {
    String haystack = (post.getTitle()+" "+post.getDescription()+" "+post.getPickupLocationName()).toLowerCase(Locale.ROOT);
    for (NotificationPreference p : preferences.findAll()) {
      if (!p.user.getActive() || p.user.getId().equals(post.getOwner().getId())) continue;
      boolean category = Arrays.asList(p.categories.split(",")).contains(post.getCategory().name());
      boolean keyword = Arrays.stream(p.keywords.split("[,\\s]+")) .filter(s -> !s.isBlank()).anyMatch(s -> haystack.contains(s.toLowerCase(Locale.ROOT)));
      if (category || keyword) save(p.user, post.getOwner(), "มีอาหารใหม่ที่อาจถูกใจ", post.getTitle()+" · "+post.getPickupLocationName(), "/posts/"+post.getId(), "interest-post:"+post.getId());
    }
  }
}
