package com.kku.foodshare.service;

public interface PickupCodeService {
  String generate();

  String encrypt(String code);

  String decrypt(String encrypted);

  String hash(String code);

  boolean matches(String code, String hash);
}
