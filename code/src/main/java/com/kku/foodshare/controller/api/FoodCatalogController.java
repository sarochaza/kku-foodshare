package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.request.FoodPostRequest;
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
      Authentication a) {
    return service.search(q, category, sort, lat, lng, now, page, size, CurrentIdentity.email(a));
  }

  @GetMapping("/food-posts/map")
  public PageView<PostView> map(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "") String category,
      @RequestParam(defaultValue = "false") boolean now,
      Authentication a) {
    return service.search(q, category, "expiry", null, null, now, 0, 200, CurrentIdentity.email(a));
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

  @DeleteMapping("/food-posts/{id}")
  public ResponseEntity<Void> delete(@PathVariable long id, Authentication a) {
    service.delete(CurrentIdentity.email(a), id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/food-posts/{id}/images", consumes = "multipart/form-data")
  public PostView image(@PathVariable long id, @RequestParam MultipartFile file, Authentication a) {
    return service.image(CurrentIdentity.email(a), id, file);
  }

  @GetMapping("/me/posts")
  public PageView<PostView> mine(@RequestParam(defaultValue = "0") int page, Authentication a) {
    return service.mine(CurrentIdentity.email(a), page);
  }

  @GetMapping("/stats")
  public java.util.Map<String, Long> stats() {
    return service.stats();
  }
}
