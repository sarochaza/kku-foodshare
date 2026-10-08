package com.kku.foodshare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_comments")
public class PostComment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  public FoodPost post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  public User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id")
  public PostComment parent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reply_to_comment_id")
  public PostComment replyTo;

  public Long getId() { return id; }
  public User getAuthor() { return author; }
  public LocalDateTime getDeletedAt() { return deletedAt; }

  @Column(nullable = false, length = 800)
  public String body;

  @Column(nullable = false)
  public LocalDateTime createdAt;

  public LocalDateTime deletedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by_id")
  public User deletedBy;
}
