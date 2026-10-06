package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.PageView;
import java.time.LocalDateTime;

public interface NotificationService {
  record View(
      Long id, String title, String message, String href, LocalDateTime createdAt, boolean read) {}

  PageView<View> list(String email, int page);

  void read(String email, long id);

  long unread(String email);
}
