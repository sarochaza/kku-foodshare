package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.SavedPost;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
  boolean existsByUserIdAndPostId(Long userId, Long postId);
  Optional<SavedPost> findByUserIdAndPostId(Long userId, Long postId);
  Page<SavedPost> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  @org.springframework.data.jpa.repository.Query("select s from SavedPost s where s.post.availableUntil > :from and s.post.availableUntil <= :until and s.post.status in :statuses")
  java.util.List<SavedPost> findNearDeadline(@org.springframework.data.repository.query.Param("from") LocalDateTime from, @org.springframework.data.repository.query.Param("until") LocalDateTime until, @org.springframework.data.repository.query.Param("statuses") Collection<com.kku.foodshare.domain.entity.FoodPostStatus> statuses);
}
