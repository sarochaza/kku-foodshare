package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.FoodPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodPostRepository extends JpaRepository<FoodPost, Long> {
}