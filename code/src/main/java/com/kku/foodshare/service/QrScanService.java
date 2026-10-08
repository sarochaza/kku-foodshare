package com.kku.foodshare.service;

public interface QrScanService {
  String decode(byte[] bytes);
}
