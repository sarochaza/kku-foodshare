package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.response.ProfileImageResponse;
import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.ProfileImageService;
import com.kku.foodshare.service.ReservationService;
import java.net.URI;
import java.util.Optional;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationMediaController {
  private final ReservationService reservations;
  private final ProfileImageService profiles;

  public ReservationMediaController(
      ReservationService reservations, ProfileImageService profiles) {
    this.reservations = reservations;
    this.profiles = profiles;
  }

  @GetMapping("/{id}/member-photo")
  public ResponseEntity<?> memberPhoto(@PathVariable long id, Authentication authentication) {
    var reservation = reservations.get(CurrentIdentity.email(authentication), id);
    Optional<ProfileImageResponse> image = profiles.getImageByUserId(reservation.memberId());
    if (image.isEmpty())
      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create("/images/default-profile.png"))
          .build();
    var result = image.get();
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .contentType(MediaType.parseMediaType(result.contentType()))
        .body(result.imageData());
  }
}
