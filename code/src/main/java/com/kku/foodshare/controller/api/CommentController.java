package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1")
public class CommentController {
  public record Input(@NotBlank @Size(max=800) String body) {}
  private final CommentService service;
  public CommentController(CommentService service){this.service=service;}
  @GetMapping("/food-posts/{postId}/comments") public PageView<CommentService.View> list(@PathVariable long postId,@RequestParam(defaultValue="0") int page,Authentication a){return service.list(postId,CurrentIdentity.email(a),page);}
  @PostMapping("/food-posts/{postId}/comments") public ResponseEntity<CommentService.View> add(@PathVariable long postId,@Valid @RequestBody Input input,Authentication a){var v=service.add(CurrentIdentity.email(a),postId,input.body());return ResponseEntity.status(HttpStatus.CREATED).body(v);}
  @DeleteMapping("/comments/{id}") public ResponseEntity<Void> remove(@PathVariable long id,Authentication a){service.remove(CurrentIdentity.email(a),id);return ResponseEntity.noContent().build();}
}
