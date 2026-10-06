package com.kku.foodshare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  public FoodPost post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reporter_id", nullable = false)
  public User reporter;

  @Column(nullable = false, length = 1000)
  public String reason;

  @Column(nullable = false, length = 20)
  public String status;

  @Column(length = 1000)
  public String resolution;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewer_id")
  public User reviewer;

  @Column(nullable = false)
  public LocalDateTime createdAt;

  public LocalDateTime resolvedAt;
  @Version public Long version;
}
