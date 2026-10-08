package com.kku.foodshare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.EmailService;
import com.kku.foodshare.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {"app.mail.enabled=true", "app.base-url=http://localhost:8081", "spring.mail.host=localhost"})
@AutoConfigureMockMvc
@Transactional
class PasswordResetJourneyTest {
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;
  @Autowired PasswordResetService resets;
  @MockitoBean EmailService mail;

  @Test
  void requestEmailResetAndLoginWithNewPassword() throws Exception {
    User member = new User();
    member.setEmail("reset-journey@test.local"); member.setDisplayName("Reset Tester");
    member.setPassword(encoder.encode("OldPassword123!")); users.saveAndFlush(member);
    mvc.perform(get("/forgot-password")).andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("ส่งลิงก์ตั้งรหัสผ่าน")));
    mvc.perform(post("/forgot-password").with(csrf()).param("email", member.getEmail()))
        .andExpect(redirectedUrl("/forgot-password?sent"));
    var url = ArgumentCaptor.forClass(String.class);
    verify(mail).sendPasswordResetEmail(eq(member.getEmail()), url.capture());
    assertTrue(url.getValue().startsWith("http://localhost:8081/reset-password?token="));
    String token = url.getValue().substring(url.getValue().indexOf("token=") + 6);
    assertTrue(resets.isTokenValid(token));
    mvc.perform(get("/reset-password").param("token", token)).andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("บันทึกรหัสผ่านใหม่")));
    mvc.perform(post("/reset-password").with(csrf()).param("token", token)
        .param("password", "NewPassword123!").param("confirmPassword", "different"))
        .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("รหัสผ่านทั้งสองช่องไม่ตรงกัน")));
    assertTrue(resets.isTokenValid(token));
    mvc.perform(post("/reset-password").with(csrf()).param("token", token)
        .param("password", "NewPassword123!").param("confirmPassword", "NewPassword123!"))
        .andExpect(redirectedUrl("/login?resetSuccess"));
    assertFalse(resets.isTokenValid(token));
    assertTrue(encoder.matches("NewPassword123!", users.findById(member.getId()).orElseThrow().getPassword()));
    mvc.perform(post("/login").with(csrf()).param("email", member.getEmail()).param("password", "OldPassword123!"))
        .andExpect(redirectedUrl("/login?error"));
    mvc.perform(post("/login").with(csrf()).param("email", member.getEmail()).param("password", "NewPassword123!"))
        .andExpect(redirectedUrl("/home"));
    mvc.perform(get("/login?resetSuccess")).andExpect(content().string(org.hamcrest.Matchers.containsString("ตั้งรหัสผ่านใหม่สำเร็จแล้ว")));
  }

  @Test
  void unknownEmailReturnsSameConfirmationWithoutSendingMail() throws Exception {
    mvc.perform(post("/forgot-password").with(csrf()).param("email", "unknown-reset@test.local"))
        .andExpect(redirectedUrl("/forgot-password?sent"));
    verify(mail, never()).sendPasswordResetEmail(any(), any());
  }

  @Test
  void invalidTokenCannotChangePasswordAndCsrfIsRequired() throws Exception {
    mvc.perform(get("/reset-password").param("token", "invalid"))
        .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("ลิงก์นี้หมดอายุหรือถูกใช้ไปแล้ว")));
    mvc.perform(post("/reset-password").with(csrf()).param("token", "invalid")
        .param("password", "NewPassword123!").param("confirmPassword", "NewPassword123!"))
        .andExpect(status().isOk());
    mvc.perform(post("/forgot-password").param("email", "unknown@test.local"))
        .andExpect(status().isForbidden());
  }
}
