package com.kku.foodshare.controller.web;

import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {
  private final FoodCatalogService catalog;
  private final MemberService members;

  public PageController(FoodCatalogService catalog, MemberService members) {
    this.catalog = catalog;
    this.members = members;
  }

  @GetMapping("/explore")
  public String explore() {
    return "explore";
  }

  @GetMapping("/posts/new")
  public String create(Authentication a, Model m) {
    members.require(CurrentIdentity.email(a));
    m.addAttribute("postId", 0);
    return "editor";
  }

  @GetMapping("/posts/{id}")
  public String detail(@PathVariable long id, Authentication a, Model m) {
    m.addAttribute("post", catalog.get(id, CurrentIdentity.email(a)));
    m.addAttribute("postId", id);
    return "detail";
  }

  @GetMapping("/posts/{id}/edit")
  public String edit(@PathVariable long id, Authentication a, Model m) {
    members.require(CurrentIdentity.email(a));
    var p = catalog.get(id, CurrentIdentity.email(a));
    if (!p.mine()) throw Problem.forbidden();
    m.addAttribute("postId", id);
    return "editor";
  }

  @GetMapping("/account/posts")
  public String mine() {
    return "my-posts";
  }

  @GetMapping("/reservations")
  public String reservations() {
    return "reservations";
  }

  @GetMapping("/notifications")
  public String notifications() {
    return "notifications";
  }

  @GetMapping("/admin")
  public String admin(Authentication a) {
    members.admin(CurrentIdentity.email(a));
    return "admin";
  }
}
