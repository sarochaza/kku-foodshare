package com.kku.foodshare.controller.api;

import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
public class MemberProfileController {
  public record View(Long id, String name) {}
  private final UserRepository users;
  public MemberProfileController(UserRepository users) { this.users = users; }
  @GetMapping("/{id}") public View profile(@PathVariable long id) {
    var user = users.findById(id).orElseThrow(Problem::missing);
    if (!Boolean.TRUE.equals(user.getActive())) throw Problem.missing();
    return new View(user.getId(), user.getDisplayName());
  }
}
