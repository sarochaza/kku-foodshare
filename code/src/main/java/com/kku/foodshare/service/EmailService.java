package com.kku.foodshare.service;

public interface EmailService {

  default void ensureAvailable() {}

  void sendPasswordResetEmail(String recipient, String resetUrl);
}
