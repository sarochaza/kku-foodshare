package com.kku.foodshare.controller.api;

import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.ProfileSettingsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProfileSettingsController {
  public record Name(@NotBlank @Size(max = 80) String name) {}

  private final ProfileSettingsService service;

  public ProfileSettingsController(ProfileSettingsService service) {
    this.service = service;
  }

  @PatchMapping("/api/v1/me/profile")
  public ResponseEntity<Void> rename(@Valid @RequestBody Name n, Authentication a) {
    service.rename(CurrentIdentity.email(a), n.name());
    return ResponseEntity.noContent().build();
  }
}
