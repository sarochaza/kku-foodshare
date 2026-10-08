package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.request.CreateCommentRequest;
import com.kku.foodshare.dto.request.UpdateCommentRequest;
import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.CommentService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1")
public class CommentController {
  private final CommentService service;
  public CommentController(CommentService service){this.service=service;}

  @Operation(summary = "อ่านความคิดเห็นของโพสต์", description = "แบ่งหน้าละ 30 รายการ เรียงตามเวลาสร้างและ ID จากเก่าไปใหม่")
  @GetMapping("/food-posts/{postId}/comments")
  public PageView<CommentService.View> list(@PathVariable long postId,
      @RequestParam(defaultValue="0") int page, Authentication a) {
    return service.list(postId, CurrentIdentity.email(a), page);
  }

  @Operation(summary = "อ่านความคิดเห็นหนึ่งรายการ")
  @GetMapping("/comments/{id}")
  public CommentService.View get(@PathVariable long id, Authentication a) {
    return service.get(CurrentIdentity.email(a), id);
  }

  @Operation(summary = "เพิ่มความคิดเห็นหรือตอบกลับ", description = "ข้อความ 1–800 ตัวอักษร; parentCommentId เป็น ID ของความคิดเห็นที่ต้องการตอบกลับ")
  @PostMapping("/food-posts/{postId}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<CommentService.View> add(@PathVariable long postId,
      @Valid @RequestBody CreateCommentRequest input, Authentication a) {
    var v = service.add(CurrentIdentity.email(a), postId, input.body(), input.parentCommentId());
    return ResponseEntity.created(URI.create("/api/v1/comments/" + v.id())).body(v);
  }

  @Operation(summary = "แก้ไขความคิดเห็นของตนเอง", description = "แก้เฉพาะข้อความ ไม่เปลี่ยนผู้เขียน โพสต์ เวลาสร้าง หรือความสัมพันธ์การตอบกลับ")
  @PutMapping("/comments/{id}")
  public CommentService.View update(@PathVariable long id,
      @Valid @RequestBody UpdateCommentRequest input, Authentication a) {
    return service.update(CurrentIdentity.email(a), id, input.body());
  }

  @Operation(summary = "ลบความคิดเห็น", description = "ผู้เขียน เจ้าของโพสต์ หรือแอดมินลบได้; เก็บการตอบกลับเดิมไว้")
  @DeleteMapping("/comments/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> remove(@PathVariable long id, Authentication a) {
    service.remove(CurrentIdentity.email(a), id);
    return ResponseEntity.noContent().build();
  }
}
