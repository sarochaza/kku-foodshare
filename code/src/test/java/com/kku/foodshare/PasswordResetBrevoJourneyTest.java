package com.kku.foodshare;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.PasswordResetTokenRepository;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.EmailService;
import com.kku.foodshare.service.PasswordResetService;
import com.kku.foodshare.service.impl.BrevoEmailService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:brevo-journey;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DEFAULT_NULL_ORDERING=HIGH",
    "app.mail.enabled=true", "app.mail.provider=brevo", "app.mail.brevo-api-key=test-api-key",
    "app.mail.from=sender@foodshare.test", "app.base-url=https://kku-foodshare.onrender.com"
})
@AutoConfigureMockMvc
@Import(PasswordResetBrevoJourneyTest.ProviderTransport.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PasswordResetBrevoJourneyTest {
  private static final CapturedBrevo provider = new CapturedBrevo();
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired PasswordResetTokenRepository tokens;
  @Autowired PasswordEncoder passwords;
  @Autowired PasswordResetService resets;

  @BeforeEach void prepare() {provider.messages.clear(); provider.status.set(201);}
  @AfterAll static void close() {provider.server.stop(0);}

  @Test void brevoHttpTransportProducesAUsablePublicLinkAndSingleUsePasswordReset() throws Exception {
    User u = account();
    mvc.perform(get("/forgot-password")).andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("ส่งลิงก์ตั้งรหัสผ่าน")));
    mvc.perform(post("/forgot-password").with(csrf()).param("email", u.getEmail()))
        .andExpect(redirectedUrl("/forgot-password?sent"));
    String body = provider.messages.poll(5, TimeUnit.SECONDS);
    assertNotNull(body, "Must exercise the actual Brevo adapter over HTTP, not mock the email service");
    assertEquals("test-api-key", provider.key.get());
    var json = new tools.jackson.databind.ObjectMapper().readTree(body);
    assertEquals("sender@foodshare.test", json.path("sender").path("email").asString());
    assertEquals(u.getEmail(), json.path("to").get(0).path("email").asString());
    var link = Pattern.compile("https://kku-foodshare\\.onrender\\.com/reset-password\\?token=([A-Za-z0-9_-]+)")
        .matcher(json.path("textContent").asString());
    assertTrue(link.find()); String token = link.group(1);
    assertFalse(json.path("textContent").asString().contains("localhost"));
    assertTrue(resets.isTokenValid(token));
    mvc.perform(post("/reset-password").with(csrf()).param("token", token)
        .param("password", "AfterReset123!").param("confirmPassword", "AfterReset123!"))
        .andExpect(redirectedUrl("/login?resetSuccess"));
    assertFalse(resets.isTokenValid(token));
    mvc.perform(post("/login").with(csrf()).param("email", u.getEmail()).param("password", "BeforeReset123!"))
        .andExpect(redirectedUrl("/login?error"));
    mvc.perform(post("/login").with(csrf()).param("email", u.getEmail()).param("password", "AfterReset123!"))
        .andExpect(redirectedUrl("/home"));
  }

  @Test void unknownAccountsAndProviderFailureDoNotExposeAccountExistenceOrLeaveAUsableToken() throws Exception {
    mvc.perform(post("/forgot-password").with(csrf()).param("email", UUID.randomUUID() + "@unknown.test"))
        .andExpect(redirectedUrl("/forgot-password?sent"));
    assertTrue(provider.messages.isEmpty());
    User u = account(); provider.status.set(401);
    mvc.perform(post("/forgot-password").with(csrf()).param("email", u.getEmail()))
        .andExpect(redirectedUrl("/forgot-password?sent"));
    assertNotNull(provider.messages.poll(5, TimeUnit.SECONDS));
    assertTrue(tokens.findAll().stream().noneMatch(t -> t.getUser().getId().equals(u.getId())), "An undelivered reset token must be removed");
    assertTrue(passwords.matches("BeforeReset123!", users.findById(u.getId()).orElseThrow().getPassword()));
  }
  private User account() {
    var u = new User(); u.setEmail(UUID.randomUUID() + "@brevo.test"); u.setDisplayName("Brevo reset journey");
    u.setPassword(passwords.encode("BeforeReset123!")); return users.saveAndFlush(u);
  }
  @TestConfiguration static class ProviderTransport {
    @Bean @Primary EmailService capturedBrevoTransport() {
      return new BrevoEmailService(HttpClient.newHttpClient(), provider.uri, "test-api-key", "sender@foodshare.test");
    }
  }
  private static final class CapturedBrevo {
    final HttpServer server; final URI uri;
    final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    final AtomicReference<String> key = new AtomicReference<>();
    final AtomicInteger status = new AtomicInteger(201);
    CapturedBrevo() {
      try {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v3/smtp/email");
        server.createContext("/v3/smtp/email", e -> {
          key.set(e.getRequestHeaders().getFirst("api-key"));
          messages.add(new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          e.sendResponseHeaders(status.get(), -1); e.close();
        }); server.start();
      } catch (IOException failure) {throw new java.io.UncheckedIOException(failure);}
    }
  }
}
