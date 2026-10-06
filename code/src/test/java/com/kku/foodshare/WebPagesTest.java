package com.kku.foodshare;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebPagesTest {
  @Autowired MockMvc mvc;

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
}
