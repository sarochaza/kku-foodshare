package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.PageView;
import java.time.LocalDateTime;

public interface ModerationService {
  record ReportView(
      Long id,
      Long postId,
      Long commentId,
      String title,
      String reporter,
      String reason,
      String status,
      String resolution,
      LocalDateTime createdAt) {}

  record UserView(Long id, String name, String email, boolean active, String role) {}

  ReportView report(String email, long postId, Long commentId, String reason);

  PageView<ReportView> reports(String email, int page);

  ReportView resolve(String email, long id, String reason, boolean closePost);

  PageView<UserView> users(String email, int page);

  void active(String email, long userId, boolean active, String reason);
}
