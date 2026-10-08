package com.kku.foodshare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ImageStorageConfiguration {
  public ImageStorageConfiguration(@Value("${app.images.provider:local}") String provider) {
    if (!"local".equalsIgnoreCase(provider) && !"cloudinary".equalsIgnoreCase(provider))
      throw new IllegalArgumentException("IMAGE_STORAGE_PROVIDER must be local or cloudinary");
  }
}
