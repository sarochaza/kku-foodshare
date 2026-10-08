package com.kku.foodshare;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class WebPagesTest {
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;

  @Test
  void publicPagesRenderIncludingSharedFragments() throws Exception {
    for (String path :
        new String[] {
          "/", "/explore", "/login", "/register", "/forgot-password", "/reset-password"
        })
      mvc.perform(get(path))
          .andExpect(status().isOk())
          .andExpect(content().string(org.hamcrest.Matchers.containsString("lang=\"th\"")));
  }

  @Test
  void guestRootShowsTheFoodShareLandingPage() throws Exception {
    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("สร้างสังคมที่ยั่งยืน")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/images/welcome-community.webp")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("data-nav=\"map\"")))
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("id=\"home-page-size\""))))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"comments-dialog\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"image-viewer\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"guest-start\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("สร้างบัญชีฟรี")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("QR หรือรหัสรับอาหาร 6 หลัก")))
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("auth-form-panel"))));
  }

  @Test
  void guestOpeningDashboardUrlReturnsToPublicHomeInsteadOfLogin() throws Exception {
    mvc.perform(get("/home"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  @Test
  void signedInRootKeepsLandingAndDashboardOpensFoodDiscovery() throws Exception {
    User member = new User();
    member.setEmail("signed-in-home@test.local");
    member.setPassword("unused");
    member.setDisplayName("Signed In Home Tester");
    users.saveAndFlush(member);

    mvc.perform(get("/").with(user(member.getEmail()).roles("USER")))
        .andExpect(status().isOk())
        .andExpect(view().name("home"));
    mvc.perform(get("/home").with(user(member.getEmail()).roles("USER")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("มื้อดี ๆ"))))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"explore-map\"")));
    mvc.perform(get("/explore").with(user(member.getEmail()).roles("USER")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("มื้อดี ๆ"))))
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("data-nav=\"home\""))))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("data-nav=\"editor\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"explore-map\"")));
  }

  @Test
  @Transactional
  void loggingOutReturnsToPublicHome() throws Exception {
    User user = new User();
    user.setEmail("logout-page@test.local");
    user.setPassword("unused");
    user.setDisplayName("Logout Tester");
    users.saveAndFlush(user);

    mvc.perform(post("/logout").with(user(user.getEmail()).roles("USER")).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  @Test
  void csrfIsRequiredForMutations() throws Exception {
    mvc.perform(
            post("/register")
                .param("email", "csrf@test.local")
                .param("displayName", "tester")
                .param("password", "Password123!"))
        .andExpect(status().isForbidden());
  }

  @Test
  void guestCannotOpenPostEditor() throws Exception {
    mvc.perform(get("/posts/new"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  @Transactional
  void signedInUserCanRenderAccountPage() throws Exception {
    User user = new User();
    user.setEmail("account-page@test.local");
    user.setPassword("unused");
    user.setDisplayName("Account Page Tester");
    users.saveAndFlush(user);

    mvc.perform(get("/account").with(user(user.getEmail()).roles("USER")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("บัญชีของฉัน")));
  }

  @Test
  @Transactional
  void signedInHeaderOffersCompactProfileMenuAndProtectedLogout() throws Exception {
    User user = new User();
    user.setEmail("profile-menu@test.local");
    user.setPassword("unused");
    user.setDisplayName("Profile Menu Tester");
    users.saveAndFlush(user);

    mvc.perform(get("/explore").with(user(user.getEmail()).roles("USER")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("profile-menu-button")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"profile-menu\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/reservations\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/account/posts\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/notifications\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/logout\"")));
    mvc.perform(get("/explore").with(user(user.getEmail()).roles("USER")))
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("id=\"guest-start\""))));
  }

  @Test
  @Transactional
  void dashboardAndExploreRenderTheSameSharingMapWithSidebarAndPreview() throws Exception {
    User member = new User();
    member.setEmail("shared-map-page@test.local");
    member.setPassword("unused");
    member.setDisplayName("Shared Map Tester");
    users.saveAndFlush(member);
    for (String path : new String[] {"/home", "/explore"}) {
      mvc.perform(get(path).param("view", "map").with(user(member.getEmail()).roles("USER")))
          .andExpect(status().isOk())
          .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"map-shell map-shell-rich\"")))
          .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"explore-map-aside\"")))
          .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"explore-map-preview\"")))
          .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"manual-location-button\"")));
    }
  }

  @Test
  @Transactional
  void ownerWorkspaceIncludesQrScannerAndManagementShortcuts() throws Exception {
    User user = new User();
    user.setEmail("owner-workspace@test.local");
    user.setPassword("unused");
    user.setDisplayName("Owner Workspace Tester");
    users.saveAndFlush(user);

    mvc.perform(get("/account/posts").with(user(user.getEmail()).roles("USER")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("owner-scan-any")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("scan-video")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("scan-review")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("owner-shortcut")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("โพสต์ของฉัน")));
  }
}
