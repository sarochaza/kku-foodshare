package com.kku.foodshare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  public User user;

  @Column(nullable = false, length = 100)
  public String categories = "";

  @Column(nullable = false, length = 300)
  public String keywords = "";

  @Column(nullable = false)
  public LocalDateTime updatedAt;
}
