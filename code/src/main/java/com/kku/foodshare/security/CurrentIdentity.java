package com.kku.foodshare.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

public final class CurrentIdentity {
  private CurrentIdentity() {}

  public static String email(Authentication a) {
    if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) return null;
    return a.getPrincipal() instanceof OAuth2User u ? u.getAttribute("email") : a.getName();
  }
}
