package com.kku.foodshare.controller.api;

import com.kku.foodshare.service.FoodCatalogService;
import com.kku.foodshare.service.ProfileImageService;
import com.kku.foodshare.dto.response.ProfileImageResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/food-posts")
public class PostProfileController {
  private final FoodCatalogService posts; private final ProfileImageService profiles;
  public PostProfileController(FoodCatalogService posts, ProfileImageService profiles) { this.posts=posts; this.profiles=profiles; }
  @GetMapping("/{id}/owner-photo") public ResponseEntity<?> ownerPhoto(@PathVariable long id) {
    var post=posts.get(id, null); var image=profiles.getImageByUserId(post.ownerId());
    if (image.isEmpty()) return ResponseEntity.status(HttpStatus.FOUND).location(java.net.URI.create("/images/default-profile.png")).build();
    ProfileImageResponse result=image.get();
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType(result.contentType())).body(result.imageData());
  }
}
