package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.PageView;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {
  record View(
      Long id, String title, String message, String href, Long actorId, String actorName, LocalDateTime createdAt, boolean read) {}

  PageView<View> list(String email, int page);

  void read(String email, long id);

  long unread(String email);

  record Preferences(List<String> categories, String keywords) {}
  Preferences preferences(String email);
  Preferences updatePreferences(String email, List<String> categories, String keywords);
  void notifyInterested(com.kku.foodshare.domain.entity.FoodPost post);
}
