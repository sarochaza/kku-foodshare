package com.kku.foodshare.repository;

import com.kku.foodshare.domain.FoodPost;
import com.kku.foodshare.domain.FoodPostStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodPostRepository extends JpaRepository<FoodPost, Long>{
    List<FoodPost> findByTitleContainingIgnoreCase(String title);
    List<FoodPost> findByStatus(FoodPostStatus status);
}