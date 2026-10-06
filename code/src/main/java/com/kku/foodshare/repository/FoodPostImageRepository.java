package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.FoodPostImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodPostImageRepository extends JpaRepository<FoodPostImage, Long> {
  Optional<FoodPostImage> findByPostId(Long postId);
}
