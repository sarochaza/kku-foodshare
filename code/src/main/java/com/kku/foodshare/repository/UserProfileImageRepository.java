package com.kku.foodshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kku.foodshare.domain.entity.UserProfileImage;

public interface UserProfileImageRepository
        extends JpaRepository<UserProfileImage, Long> {
}