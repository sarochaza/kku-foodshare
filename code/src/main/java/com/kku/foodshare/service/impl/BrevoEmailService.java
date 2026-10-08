package com.kku.foodshare.service.impl;

import com.kku.foodshare.service.EmailService;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
@ConditionalOnExpression("'${app.mail.provider:smtp}' == 'brevo'")
public class BrevoEmailService implements EmailService {
  private final HttpClient client;
  private final URI endpoint;
  private final String apiKey;
  private final String sender;

  @Autowired
  public BrevoEmailService(@Value("${app.mail.brevo-api-key:}") String apiKey,
      @Value("${app.mail.from:no-reply@localhost}") String sender) {
    this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
        URI.create("https://api.brevo.com/v3/smtp/email"), apiKey, sender);
  }

  public BrevoEmailService(HttpClient client, URI endpoint, String apiKey, String sender) {
    this.client = client; this.endpoint = endpoint; this.apiKey = apiKey; this.sender = sender;
  }

  @Override public void ensureAvailable() {
    if (apiKey.isBlank() || sender.isBlank() || sender.endsWith("@localhost"))
      throw new com.kku.foodshare.exception.Problem(503, "ระบบส่งอีเมลยังไม่พร้อม กรุณาติดต่อผู้ดูแล");
  }

  @Override public void sendPasswordResetEmail(String recipient, String resetUrl) {
    ensureAvailable();
    var body = Map.of("sender", Map.of("name", "KKU FoodShare", "email", sender),
        "to", List.of(Map.of("email", recipient)),
        "subject", "ตั้งรหัสผ่านใหม่ | KKU FoodShare",
        "textContent", "ตั้งรหัสผ่านใหม่สำหรับ KKU FoodShare\n\n" + resetUrl
            + "\n\nลิงก์นี้ใช้ได้ 15 นาทีและใช้ได้ครั้งเดียว หากคุณไม่ได้ขอเปลี่ยนรหัสผ่าน สามารถละเว้นอีเมลนี้ได้");
    try {
      String json = new tools.jackson.databind.ObjectMapper().writeValueAsString(body);
      var request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(10))
          .header("api-key", apiKey).header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(json)).build();
      var response = client.send(request, HttpResponse.BodyHandlers.discarding());
      if (response.statusCode() < 200 || response.statusCode() >= 300)
        throw new MailSendException("Email provider rejected delivery (HTTP " + response.statusCode() + ")");
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt(); throw new MailSendException("Email delivery interrupted");
    } catch (java.io.IOException failure) {
      throw new MailSendException("Unable to reach email provider");
    }
  }
}
