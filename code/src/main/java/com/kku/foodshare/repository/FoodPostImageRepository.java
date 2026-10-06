package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.FoodPostImage;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodPostImageRepository extends JpaRepository<FoodPostImage, Long> {
  List<FoodPostImage> findAllByPostIdOrderBySortOrderAscIdAsc(Long postId);

  long countByPostId(Long postId);

  Optional<FoodPostImage> findByIdAndPostId(Long id, Long postId);
}
