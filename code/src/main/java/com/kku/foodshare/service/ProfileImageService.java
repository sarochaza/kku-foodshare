package com.kku.foodshare.service;

import java.util.Optional;

import com.kku.foodshare.dto.response.ProfileImageResponse;

public interface ProfileImageService {

    void updateImage(String email, byte[] imageBytes);

    Optional<ProfileImageResponse> getImage(String email);
}