package com.kku.foodshare.security;

import com.kku.foodshare.service.GoogleUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final GoogleUserService googleUserService;

  public GoogleOAuth2SuccessHandler(GoogleUserService googleUserService) {

    this.googleUserService = googleUserService;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

    if (!Boolean.TRUE.equals(oauth2User.getAttribute("email_verified"))) {
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
      request.getSession().invalidate();
      response.sendRedirect("/login?error");
      return;
    }

    String email = oauth2User.getAttribute("email");

    String name = oauth2User.getAttribute("name");

    googleUserService.findOrCreateGoogleUser(email, name);

    response.sendRedirect("/home");
  }
}
