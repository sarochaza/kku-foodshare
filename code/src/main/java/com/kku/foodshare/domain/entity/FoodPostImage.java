package com.kku.foodshare.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "food_post_images")
public class FoodPostImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false, unique = true)
  public FoodPost post;

  @Column(nullable = false, length = 100)
  public String filename;
}
