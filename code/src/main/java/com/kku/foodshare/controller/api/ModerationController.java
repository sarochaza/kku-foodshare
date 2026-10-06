package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.ModerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ModerationController {
  public record ReportInput(@Min(1) long postId, @NotBlank @Size(max = 1000) String reason) {}

  public record Resolve(@NotBlank @Size(max = 1000) String reason, boolean closePost) {}

  public record Active(@NotBlank @Size(max = 1000) String reason, boolean active) {}

  private final ModerationService service;

  public ModerationController(ModerationService service) {
    this.service = service;
  }

  @PostMapping("/reports")
  public ResponseEntity<ModerationService.ReportView> report(
      @Valid @RequestBody ReportInput r, Authentication a) {
    return ResponseEntity.status(201)
        .body(service.report(CurrentIdentity.email(a), r.postId(), r.reason()));
  }

  @GetMapping("/admin/reports")
  public PageView<ModerationService.ReportView> reports(
      @RequestParam(defaultValue = "0") int page, Authentication a) {
    return service.reports(CurrentIdentity.email(a), page);
  }

  @PatchMapping("/admin/reports/{id}")
  public ModerationService.ReportView resolve(
      @PathVariable long id, @Valid @RequestBody Resolve r, Authentication a) {
    return service.resolve(CurrentIdentity.email(a), id, r.reason(), r.closePost());
  }

  @GetMapping("/admin/users")
  public PageView<ModerationService.UserView> users(
      @RequestParam(defaultValue = "0") int page, Authentication a) {
    return service.users(CurrentIdentity.email(a), page);
  }

  @PatchMapping("/admin/users/{id}")
  public ResponseEntity<Void> active(
      @PathVariable long id, @Valid @RequestBody Active r, Authentication a) {
    service.active(CurrentIdentity.email(a), id, r.active(), r.reason());
    return ResponseEntity.noContent().build();
  }
}
