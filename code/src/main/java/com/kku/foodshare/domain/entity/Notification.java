package com.kku.foodshare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  public User user;

  @Column(nullable = false, length = 160)
  public String title;

  @Column(nullable = false, length = 500)
  public String message;

  @Column(nullable = false, length = 255)
  public String href;

  @Column(nullable = false)
  public LocalDateTime createdAt;

  public LocalDateTime readAt;
}
