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

    mvc.perform(get("/").with(user(user.getEmail()).roles("USER")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("profile-menu-button")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"profile-menu\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/reservations\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/account/posts\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/notifications\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/logout\"")));
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
