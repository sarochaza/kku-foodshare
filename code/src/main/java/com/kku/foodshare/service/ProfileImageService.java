package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.ProfileImageResponse;
import java.util.Optional;

public interface ProfileImageService {

  void updateImage(String email, byte[] imageBytes);

  Optional<ProfileImageResponse> getImage(String email);
}
