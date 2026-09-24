package com.kku.foodshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kku.foodshare.domain.FoodPost;

public interface FoodPostRepository extends JpaRepository<FoodPost, Long> {
}