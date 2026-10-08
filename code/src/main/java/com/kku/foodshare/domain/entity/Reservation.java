package com.kku.foodshare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "reservations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "request_key"}))
public class Reservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  public FoodPost post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  public User member;

  @Column(nullable = false)
  public int quantity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  public ReservationStatus status;

  @Column(name = "request_key", nullable = false, length = 64)
  public String requestKey;

  @Column(nullable = false, length = 100)
  public String pickupHash;

  @Column(nullable = false, length = 255)
  public String pickupEncrypted;

  @Column(nullable = false)
  public int failedAttempts;

  public LocalDateTime lockedUntil;

  @Column(nullable = false)
  public LocalDateTime createdAt;

  @Column(nullable = false)
  public LocalDateTime updatedAt;

  @Version public Long version;
}
