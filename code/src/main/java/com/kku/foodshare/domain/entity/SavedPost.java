package com.kku.foodshare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_posts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
public class SavedPost {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private FoodPost post;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public Long getId() { return id; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public FoodPost getPost() { return post; }
  public void setPost(FoodPost post) { this.post = post; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
