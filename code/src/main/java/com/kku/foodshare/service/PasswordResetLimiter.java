package com.kku.foodshare.service;

public interface PasswordResetLimiter {
  void passwordReset(String address);
}
