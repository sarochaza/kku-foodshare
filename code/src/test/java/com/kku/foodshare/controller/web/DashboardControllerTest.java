package com.kku.foodshare.controller.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kku.foodshare.dto.response.UserProfileResponse;
import com.kku.foodshare.service.UserProfileService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

class DashboardControllerTest {

  // ทดสอบว่าเปิดหน้า Dashboard ถูกต้อง
  @Test
  void dashboardShouldAddEmailAccountProfileToModel() {

    UserProfileService userProfileService = mock(UserProfileService.class);

    UserProfileResponse profile =
        new UserProfileResponse("ชมพู่ เสาทอง", "sarocha@kku.ac.th", "บัญชีอีเมล");

    when(userProfileService.getProfile("sarocha@kku.ac.th", "บัญชีอีเมล")).thenReturn(profile);

    DashboardController controller = new DashboardController(userProfileService);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("sarocha@kku.ac.th", "password");

    Model model = new ExtendedModelMap();

    String viewName = controller.dashboard(authentication, model);

    assertEquals("dashboard", viewName);

    assertSame(profile, model.getAttribute("currentUser"));

    verify(userProfileService).getProfile("sarocha@kku.ac.th", "บัญชีอีเมล");
  }

  @Test
  void dashboardShouldAddGoogleAccountProfileToModel() {

    UserProfileService userProfileService = mock(UserProfileService.class);

    UserProfileResponse profile =
        new UserProfileResponse("Sarocha Saothong", "sarocha@gmail.com", "บัญชี Google");

    when(userProfileService.getProfile("sarocha@gmail.com", "บัญชี Google")).thenReturn(profile);

    OAuth2User oauth2User =
        new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of("sub", "10987654321", "email", "sarocha@gmail.com", "name", "Sarocha Saothong"),
            "sub");

    Authentication authentication =
        new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "google");

    Model model = new ExtendedModelMap();

    DashboardController controller = new DashboardController(userProfileService);

    String viewName = controller.dashboard(authentication, model);

    assertEquals("dashboard", viewName);

    assertSame(profile, model.getAttribute("currentUser"));

    verify(userProfileService).getProfile("sarocha@gmail.com", "บัญชี Google");
  }
}
