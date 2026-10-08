package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.response.*;
import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReservationController {
  public record Quantity(@Min(1) @Max(10000) int quantity) {}

  public record Code(@NotBlank String code) {}
  public record PreferenceInput(java.util.List<String> categories, @Size(max = 300) String keywords) {}

  private final ReservationService service;
  private final NotificationService notifications;

  public ReservationController(ReservationService service, NotificationService notifications) {
    this.service = service;
    this.notifications = notifications;
  }

  @PostMapping("/food-posts/{id}/reservations")
  public ResponseEntity<ReservationView> reserve(
      @PathVariable long id,
      @Valid @RequestBody Quantity r,
      @RequestHeader("Idempotency-Key") String key,
      Authentication a) {
    var v = service.reserve(CurrentIdentity.email(a), id, r.quantity(), key);
    return ResponseEntity.created(java.net.URI.create("/api/v1/reservations/" + v.id())).body(v);
  }

  @GetMapping("/food-posts/{id}/reservations")
  public List<ReservationView> postReservations(@PathVariable long id, Authentication a) {
    return service.forPost(CurrentIdentity.email(a), id);
  }

  @GetMapping("/food-posts/{id}/my-reservation")
  public ResponseEntity<ReservationView> mineForPost(@PathVariable long id, Authentication a) {
    var result = service.mineForPost(CurrentIdentity.email(a), id);
    return result == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
  }

  @GetMapping("/reservations/{id}")
  public ReservationView get(@PathVariable long id, Authentication a) {
    return service.get(CurrentIdentity.email(a), id);
  }

  @PutMapping("/reservations/{id}")
  public ReservationView update(
      @PathVariable long id, @Valid @RequestBody Quantity r, Authentication a) {
    return service.changeQuantity(CurrentIdentity.email(a), id, r.quantity());
  }

  @DeleteMapping("/reservations/{id}")
  public ResponseEntity<Void> cancel(@PathVariable long id, Authentication a) {
    service.cancel(CurrentIdentity.email(a), id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reservations/{id}/collection")
  public ReservationView collect(
      @PathVariable long id, @Valid @RequestBody Code r, Authentication a) {
    return service.collect(CurrentIdentity.email(a), id, r.code());
  }

  @GetMapping("/me/reservations")
  public PageView<ReservationView> mine(
      @RequestParam(defaultValue = "0") int page, Authentication a) {
    return service.mine(CurrentIdentity.email(a), page);
  }

  @GetMapping("/me/notifications")
  public PageView<NotificationService.View> notifications(
      @RequestParam(defaultValue = "0") int page, Authentication a) {
    return notifications.list(CurrentIdentity.email(a), page);
  }

  @PostMapping("/me/notifications/{id}/read")
  public ResponseEntity<Void> read(@PathVariable long id, Authentication a) {
    notifications.read(CurrentIdentity.email(a), id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me/notifications/unread")
  public java.util.Map<String, Long> unread(Authentication a) { return java.util.Map.of("count", notifications.unread(CurrentIdentity.email(a))); }

  @GetMapping("/me/notification-preferences")
  public NotificationService.Preferences preferences(Authentication a) { return notifications.preferences(CurrentIdentity.email(a)); }

  @PutMapping("/me/notification-preferences")
  public NotificationService.Preferences preferences(@Valid @RequestBody PreferenceInput input, Authentication a) { return notifications.updatePreferences(CurrentIdentity.email(a), input.categories(), input.keywords()); }
}
