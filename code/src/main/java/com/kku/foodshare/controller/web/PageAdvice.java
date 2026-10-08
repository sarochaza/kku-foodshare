package com.kku.foodshare.controller.web;

import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.MemberService;
import com.kku.foodshare.service.NotificationService;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice(basePackages = "com.kku.foodshare.controller.web")
public class PageAdvice {
  public record Viewer(Long id, String name, String email, String role) {}

  private final MemberService members;
  private final NotificationService notifications;
  private final ObjectProvider<ClientRegistrationRepository> clients;
  private final String tile;
  private final String attribution;
  private final boolean mail;
  private final boolean demo;

  public PageAdvice(
      MemberService members, NotificationService notifications,
      ObjectProvider<ClientRegistrationRepository> clients,
      @Value("${app.map.tile-url}") String tile,
      @Value("${app.map.attribution}") String attribution,
      @Value("${app.mail.enabled:false}") boolean mail,
      @Value("${app.demo:false}") boolean demo) {
    this.members = members;
    this.notifications = notifications;
    this.clients = clients;
    this.tile = tile;
    this.attribution = attribution;
    this.mail = mail;
    this.demo = demo;
  }

  @ModelAttribute
  public void common(Authentication a, Model m) {
    m.addAttribute("onboardingNeeded", false);
    String email = CurrentIdentity.email(a);
    if (email != null) {
      var u = members.require(email);
      m.addAttribute("onboardingNeeded", !u.isOnboardingCompleted());
      m.addAttribute(
          "viewer", new Viewer(u.getId(), u.getDisplayName(), u.getEmail(), u.getRole().name()));
      m.addAttribute("unreadCount", notifications.unread(email));
    }
    m.addAttribute("googleEnabled", clients.getIfAvailable() != null);
    m.addAttribute("mailEnabled", mail);
    m.addAttribute("tileUrl", tile);
    m.addAttribute("tileAttribution", attribution);
    m.addAttribute("demo", demo);
  }
}
