package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.Notification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  Page<Notification> findByUserIdOrderByCreatedAtDesc(Long id, Pageable page);

  long countByUserIdAndReadAtIsNull(Long id);

  boolean existsByUserIdAndDedupeKey(Long userId, String dedupeKey);
}
