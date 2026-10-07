package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.PageView;
import java.time.LocalDateTime;

public interface CommentService {
  record View(Long id, Long postId, Long authorId, String authorName, String body, LocalDateTime createdAt, boolean canDelete,
      Long parentCommentId, Long replyToCommentId, String replyToAuthorName, boolean parentDeleted) {}
  PageView<View> list(long postId, String email, int page);
  View add(String email, long postId, String body);
  View add(String email, long postId, String body, Long parentCommentId);
  void remove(String email, long id);
}
