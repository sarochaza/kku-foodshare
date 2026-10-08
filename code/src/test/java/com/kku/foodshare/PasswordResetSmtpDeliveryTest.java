package com.kku.foodshare;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.PasswordResetService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "app.mail.enabled=true", "app.mail.provider=smtp", "app.mail.from=no-reply@foodshare.test",
    "app.base-url=http://127.0.0.1:8081", "spring.mail.host=127.0.0.1",
    "spring.mail.properties.mail.smtp.auth=false", "spring.mail.properties.mail.smtp.starttls.enable=false"
})
@AutoConfigureMockMvc
@Transactional
class PasswordResetSmtpDeliveryTest {
  private static final CapturedSmtp smtp = new CapturedSmtp();
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;
  @Autowired PasswordResetService resets;

  @DynamicPropertySource
  static void smtpPort(DynamicPropertyRegistry registry) {
    registry.add("spring.mail.port", smtp::port);
  }

  @AfterAll static void stopSmtp() throws IOException { smtp.close(); }

  @Test
  void realSmtpEmailContainsAUsableLinkAndTheNewPasswordCanLogIn() throws Exception {
    // Keep rendered Thymeleaf views for the CSS/browser check, including conditional content.
    for (String name : new String[] {"home", "login", "register", "forgot-password"}) {
      var rendered = mvc.perform(get(name.equals("home") ? "/" : "/" + name)).andExpect(status().isOk()).andReturn();
      saveRenderedView(name, rendered.getResponse().getContentAsString());
    }
    var member = new User();
    member.setEmail("smtp-journey@foodshare.test");
    member.setDisplayName("SMTP journey");
    member.setPassword(encoder.encode("BeforeReset123!"));
    users.saveAndFlush(member);
    mvc.perform(get("/forgot-password")).andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("ส่งลิงก์ตั้งรหัสผ่าน")));
    mvc.perform(post("/forgot-password").with(csrf()).param("email", member.getEmail()))
        .andExpect(redirectedUrl("/forgot-password?sent"));

    String raw = smtp.inbox.poll(5, TimeUnit.SECONDS);
    assertNotNull(raw, "The actual JavaMailSender must deliver a message over SMTP");
    var message = new MimeMessage(Session.getInstance(new Properties()),
        new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));
    assertEquals(member.getEmail(), message.getAllRecipients()[0].toString());
    assertEquals("ตั้งรหัสผ่านใหม่ | KKU FoodShare", message.getSubject());
    var link = Pattern.compile("http://127\\.0\\.0\\.1:8081/reset-password\\?token=([A-Za-z0-9_-]+)")
        .matcher((String) message.getContent());
    assertTrue(link.find(), "Email must point to the published app port, not container port 8080");
    String token = link.group(1);
    assertTrue(resets.isTokenValid(token));
    var resetPage = mvc.perform(get("/reset-password").param("token", token)).andExpect(status().isOk()).andReturn();
    saveRenderedView("reset-password", resetPage.getResponse().getContentAsString());
    mvc.perform(post("/reset-password").with(csrf()).param("token", token)
        .param("password", "AfterReset123!").param("confirmPassword", "AfterReset123!"))
        .andExpect(redirectedUrl("/login?resetSuccess"));
    assertFalse(resets.isTokenValid(token));
    mvc.perform(post("/login").with(csrf()).param("email", member.getEmail()).param("password", "BeforeReset123!"))
        .andExpect(redirectedUrl("/login?error"));
    mvc.perform(post("/login").with(csrf()).param("email", member.getEmail()).param("password", "AfterReset123!"))
        .andExpect(redirectedUrl("/home"));
  }

  private void saveRenderedView(String name, String html) throws IOException {
    Path directory = Path.of("target", "ui-rendered");
    Files.createDirectories(directory);
    Files.writeString(directory.resolve(name + ".html"), html);
  }

  // A local SMTP capture fixture, similar to Mailpit. No mail leaves the test machine.
  private static final class CapturedSmtp implements AutoCloseable {
    final BlockingQueue<String> inbox = new LinkedBlockingQueue<>();
    private final ServerSocket listener;
    CapturedSmtp() {
      try { listener = new ServerSocket(0, 1, InetAddress.getLoopbackAddress()); }
      catch (IOException failure) { throw new UncheckedIOException(failure); }
      var worker = new Thread(() -> {
        while (!listener.isClosed()) {
          try (var socket = listener.accept()) { capture(socket); }
          catch (IOException failure) { if (!listener.isClosed()) throw new UncheckedIOException(failure); }
        }
      }, "test-smtp-capture");
      worker.setDaemon(true); worker.start();
    }
    int port() { return listener.getLocalPort(); }
    private void capture(Socket socket) throws IOException {
      socket.setSoTimeout(5000);
      var input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      var output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
      reply(output, "220 localhost SMTP capture");
      for (String line; (line = input.readLine()) != null;) {
        if (line.equals("DATA")) {
          reply(output, "354 End with a dot");
          var body = new StringBuilder();
          while ((line = input.readLine()) != null && !line.equals("."))
            body.append(line.startsWith("..") ? line.substring(1) : line).append("\r\n");
          inbox.add(body.toString()); reply(output, "250 Captured");
        } else if (line.equals("QUIT")) { reply(output, "221 Bye"); return; }
        else reply(output, "250 OK");
      }
    }
    private void reply(PrintWriter writer, String line) { writer.print(line + "\r\n"); writer.flush(); }
    @Override public void close() throws IOException { listener.close(); }
  }
}
