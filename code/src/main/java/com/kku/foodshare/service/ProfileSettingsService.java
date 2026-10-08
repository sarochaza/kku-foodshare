package com.kku.foodshare.service;

public interface ProfileSettingsService {
  void rename(String email, String name);

  void completeOnboarding(String email);
}
