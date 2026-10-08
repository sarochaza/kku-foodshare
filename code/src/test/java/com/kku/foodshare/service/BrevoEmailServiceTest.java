package com.kku.foodshare.service;
import static org.junit.jupiter.api.Assertions.*;
import com.kku.foodshare.service.impl.BrevoEmailService;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
class BrevoEmailServiceTest {
  @Test void sendsResetLinkThroughHttpAndRejectsProviderFailure() throws Exception {
    var payload = new AtomicReference<String>(); var key = new AtomicReference<String>();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/email", e -> {
      key.set(e.getRequestHeaders().getFirst("api-key"));
      payload.set(new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      e.sendResponseHeaders(201, -1); e.close();
    }); server.start();
    try {
      URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/email");
      var mail = new BrevoEmailService(HttpClient.newHttpClient(), uri, "test-key", "sender@example.org");
      mail.sendPasswordResetEmail("member@example.org", "https://food.example/reset-password?token=abc");
      assertEquals("test-key", key.get());
      assertTrue(payload.get().contains("member@example.org"));
      assertTrue(payload.get().contains("https://food.example/reset-password?token=abc"));
      server.removeContext("/email");
      server.createContext("/email", e -> { e.sendResponseHeaders(401, -1); e.close(); });
      assertThrows(MailSendException.class, () -> mail.sendPasswordResetEmail("member@example.org", "https://food.example/reset-password?token=abc"));
    } finally { server.stop(0); }
  }
}
