package com.kku.foodshare.controller.web;

import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.service.storage.ImageStorage;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class MediaController {
  private final ImageStorage storage;

  public MediaController(ImageStorage storage) {
    this.storage = storage;
  }

  @GetMapping("/media/{name}")
  public ResponseEntity<Resource> image(@PathVariable String name) {
    try {
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG)
          .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7)))
          .header("X-Content-Type-Options", "nosniff")
          .body(storage.load(name));
    } catch (Problem p) {
      return ResponseEntity.notFound().build();
    }
  }
}
