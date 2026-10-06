package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.dto.response.PostView;
import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.SavedPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class SavedPostController {
  private final SavedPostService saved;
  public SavedPostController(SavedPostService saved) { this.saved = saved; }

  @PutMapping("/food-posts/{id}/saved")
  public ResponseEntity<Void> save(@PathVariable long id, Authentication a) {
    saved.save(CurrentIdentity.email(a), id); return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/food-posts/{id}/saved")
  public ResponseEntity<Void> remove(@PathVariable long id, Authentication a) {
    saved.remove(CurrentIdentity.email(a), id); return ResponseEntity.noContent().build();
  }

  @GetMapping("/me/saved-posts")
  public PageView<PostView> mine(@RequestParam(defaultValue = "0") int page, Authentication a) {
    return saved.mine(CurrentIdentity.email(a), page);
  }
}
