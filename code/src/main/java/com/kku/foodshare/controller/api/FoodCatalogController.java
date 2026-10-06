package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.request.FoodPostRequest;
import com.kku.foodshare.dto.request.ExtendPostRequest;
import com.kku.foodshare.dto.response.*;
import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.FoodCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class FoodCatalogController {
  private final FoodCatalogService service;

  public FoodCatalogController(FoodCatalogService service) {
    this.service = service;
  }

  @GetMapping("/food-posts")
  public PageView<PostView> search(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "") String category,
      @RequestParam(defaultValue = "expiry") String sort,
      @RequestParam(required = false) Double lat,
      @RequestParam(required = false) Double lng,
      @RequestParam(defaultValue = "false") boolean now,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(defaultValue = "") String ownership,
      Authentication a) {
    return service.search(q, category, sort, lat, lng, now, page, size, CurrentIdentity.email(a), ownership);
  }

  @GetMapping("/food-posts/map")
  public PageView<PostView> map(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "") String category,
      @RequestParam(defaultValue = "false") boolean now,
      Authentication a) {
    return service.search(q, category, "expiry", null, null, now, 0, 200, CurrentIdentity.email(a), "");
  }

  @GetMapping("/food-posts/{id}")
  public PostView get(@PathVariable long id, Authentication a) {
    return service.get(id, CurrentIdentity.email(a));
  }

  @PostMapping("/food-posts")
  public ResponseEntity<PostView> create(@Valid @RequestBody FoodPostRequest r, Authentication a) {
    PostView p = service.create(CurrentIdentity.email(a), r);
    return ResponseEntity.created(java.net.URI.create("/api/v1/food-posts/" + p.id())).body(p);
  }

  @PutMapping("/food-posts/{id}")
  public PostView update(
      @PathVariable long id, @Valid @RequestBody FoodPostRequest r, Authentication a) {
    return service.update(CurrentIdentity.email(a), id, r);
  }

  @PostMapping("/food-posts/{id}/extend")
  public PostView extend(
      @PathVariable long id, @Valid @RequestBody ExtendPostRequest r, Authentication a) {
    return service.extend(CurrentIdentity.email(a), id, r);
  }

  @DeleteMapping("/food-posts/{id}")
  public ResponseEntity<Void> delete(@PathVariable long id, Authentication a) {
    service.delete(CurrentIdentity.email(a), id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/food-posts/{id}/images", consumes = "multipart/form-data")
  public PostView image(@PathVariable long id, @RequestParam MultipartFile file, Authentication a) {
    return service.image(CurrentIdentity.email(a), id, file);
  }

  @DeleteMapping("/food-posts/{id}/images/{imageId}")
  public ResponseEntity<Void> removeImage(@PathVariable long id, @PathVariable long imageId, Authentication a) {
    service.removeImage(CurrentIdentity.email(a), id, imageId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me/posts")
  public PageView<PostView> mine(@RequestParam(defaultValue = "0") int page, Authentication a) {
    return service.mine(CurrentIdentity.email(a), page);
  }

  @GetMapping("/members/{ownerId}/posts")
  public PageView<PostView> ownerPosts(@PathVariable long ownerId, @RequestParam(defaultValue = "0") int page, Authentication a) {
    return service.ownerPosts(ownerId, CurrentIdentity.email(a), page);
  }

  @GetMapping("/me/posts/management-summary")
  public java.util.Map<String, Long> managementSummary(Authentication a) {
    return service.managementSummary(CurrentIdentity.email(a));
  }

  @GetMapping("/stats")
  public java.util.Map<String, Long> stats() {
    return service.stats();
  }
}
