package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.response.MemberProfileResponse;
import com.kku.foodshare.service.MemberProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
public class MemberProfileController {
  private final MemberProfileService service;
  public MemberProfileController(MemberProfileService service) { this.service = service; }
  @GetMapping("/{id}") public MemberProfileResponse profile(@PathVariable long id) {
    return service.get(id);
  }
}
