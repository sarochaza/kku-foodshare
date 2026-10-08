package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.response.ProfileImageResponse;
import com.kku.foodshare.service.ProfileImageService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
public class MemberPhotoController {
  private final ProfileImageService profiles;
  public MemberPhotoController(ProfileImageService profiles) { this.profiles = profiles; }
  @GetMapping("/{id}/photo") public ResponseEntity<?> photo(@PathVariable long id) {
    var image = profiles.getImageByUserId(id);
    if (image.isEmpty()) return ResponseEntity.status(HttpStatus.FOUND).location(java.net.URI.create("/images/default-profile.png")).build();
    ProfileImageResponse result = image.get();
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType(result.contentType())).body(result.imageData());
  }
}
