package com.kku.foodshare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  public User actor;

  @Column(nullable = false, length = 80)
  public String action;

  @Column(nullable = false, length = 80)
  public String targetType;

  @Column(nullable = false)
  public Long targetId;

  @Column(nullable = false, length = 1000)
  public String reason;

  @Column(nullable = false)
  public LocalDateTime createdAt;
}
