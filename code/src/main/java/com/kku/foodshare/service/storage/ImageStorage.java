package com.kku.foodshare.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
  String store(MultipartFile file);

  Resource load(String name);

  void remove(String name);

  default java.net.URI publicUrl(String name) {
    return null;
  }
}
