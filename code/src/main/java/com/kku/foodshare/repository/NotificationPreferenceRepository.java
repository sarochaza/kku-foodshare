package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.NotificationPreference;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
  Optional<NotificationPreference> findByUserId(Long userId);
}
