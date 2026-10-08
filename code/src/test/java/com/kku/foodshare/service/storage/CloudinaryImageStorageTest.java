package com.kku.foodshare.service.storage;

import static org.junit.jupiter.api.Assertions.*;

import com.kku.foodshare.controller.web.MediaController;
import com.kku.foodshare.exception.Problem;
import com.sun.net.httpserver.HttpServer;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

class CloudinaryImageStorageTest {
  @TempDir Path directory;
  private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-08T10:00:00Z"), ZoneOffset.UTC);
  private HttpServer server;
  private URI endpoint;
  private final AtomicReference<CapturedUpload> upload = new AtomicReference<>();
  private final AtomicReference<Map<String, String>> deletion = new AtomicReference<>();
  private final AtomicInteger requests = new AtomicInteger();

  @BeforeEach void provider() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/image/");
    server.createContext("/image/upload", exchange -> {
      requests.incrementAndGet();
      CapturedUpload received = capture(exchange.getRequestHeaders().getFirst("Content-Type"), exchange.getRequestBody().readAllBytes());
      upload.set(received);
      String json = "{\"asset_id\":\"test-asset\",\"public_id\":\"" + received.fields.get("public_id")
          + "\",\"resource_type\":\"image\",\"format\":\"jpg\",\"version\":1791453600,\"secure_url\":\"https://res.cloudinary.com/demo-cloud/image/upload/example.jpg\"}";
      byte[] body = json.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, body.length); exchange.getResponseBody().write(body); exchange.close();
    });
    server.createContext("/image/destroy", exchange -> {
      requests.incrementAndGet();
      var fields = new HashMap<String, String>();
      String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      for (String pair : body.split("&")) {
        String[] parts = pair.split("=", 2);
        fields.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
      }
      deletion.set(fields);
      byte[] response = "{\"result\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, response.length); exchange.getResponseBody().write(response); exchange.close();
    });
    server.start();
  }

  @AfterEach void close() { server.stop(0); }

  private CloudinaryImageStorage storage() {
    return new CloudinaryImageStorage(new LocalImageStorage(directory.toString()), "demo-cloud", "test-api-key", "private-test-secret", CLOCK, HttpClient.newHttpClient(), endpoint);
  }

  @Test void signedUploadPreservesValidationAndStoresAResizedJpegInsteadOfOriginalBytes() throws Exception {
    String name = storage().store(png(2800, 1400));
    assertTrue(name.matches("cld-[0-9a-f-]{36}\\.jpg"));
    assertTrue(name.length() <= 100, "Fits the existing filename column; no migration needed");
    var received = upload.get();
    assertEquals("test-api-key", received.fields.get("api_key"));
    assertEquals("false", received.fields.get("overwrite"));
    assertEquals("1791453600", received.fields.get("timestamp"));
    String publicId = "kku-foodshare/" + name.substring(0, name.length() - 4);
    assertEquals(publicId, received.fields.get("public_id"));
    assertEquals(sha256("overwrite=false&public_id=" + publicId + "&timestamp=1791453600private-test-secret"), received.fields.get("signature"));
    assertFalse(received.fields.containsKey("api_secret"));
    assertTrue(received.file.length > 0);
    assertEquals(0xff, received.file[0] & 0xff); assertEquals(0xd8, received.file[1] & 0xff);
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(received.file));
    assertEquals(1400, image.getWidth()); assertEquals(700, image.getHeight());
    assertEquals(0, stagedFiles(), "Validated local staging copy must be cleaned after upload");
  }

  @Test void mediaUrlSurvivesACompletelyNewStorageInstanceWithoutTheLocalUploadDirectory() throws Exception {
    String name = storage().store(png(10, 8));
    Files.delete(directory);
    var response = new MediaController(storage()).image(name);
    assertEquals(302, response.getStatusCode().value());
    assertEquals("https://res.cloudinary.com/demo-cloud/image/upload/kku-foodshare/" + name, response.getHeaders().getLocation().toString());
    assertNull(response.getBody(), "Let the browser load CDN bytes without proxying through the sleeping app");
    assertEquals(1, requests.get(), "Opening a picture does not make a Cloudinary API request");
  }

  @Test void existingLocalImagesKeepTheirOriginalMediaAddressAndDeleteStillWorks() throws Exception {
    String oldName = new LocalImageStorage(directory.toString()).store(png(9, 8));
    var response = new MediaController(storage()).image(oldName);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(ImageIO.read(response.getBody().getInputStream()));
    storage().remove(oldName);
    assertFalse(Files.exists(directory.resolve(oldName))); assertEquals(0, requests.get());
  }

  @Test void removingACloudImageUsesSignedDestroyForOnlyThatImage() throws Exception {
    String name = storage().store(png(9, 8));
    storage().remove(name);
    var fields = deletion.get();
    String publicId = "kku-foodshare/" + name.substring(0, name.length() - 4);
    assertEquals(publicId, fields.get("public_id"));
    assertEquals("true", fields.get("invalidate"));
    assertEquals(sha256("invalidate=true&public_id=" + publicId + "&timestamp=1791453600private-test-secret"), fields.get("signature"));
    assertFalse(fields.containsKey("api_secret"));
    int before = requests.get();
    storage().remove("../../other-image");
    assertEquals(before, requests.get());
    assertEquals(404, new MediaController(storage()).image("cld-../../invalid.jpg").getStatusCode().value());
  }

  @Test void invalidOrOversizedImagesAreRejectedBeforeAnyProviderRequest() throws Exception {
    var s = storage();
    assertEquals(400, assertThrows(Problem.class, () -> s.store(new MockMultipartFile("image", "fake.jpg", "image/jpeg", "<svg/>".getBytes()))).status);
    assertEquals(400, assertThrows(Problem.class, () -> s.store(new MockMultipartFile("image", "large.jpg", "image/jpeg", new byte[5 * 1024 * 1024 + 1]))).status);
    assertEquals(400, assertThrows(Problem.class, () -> s.store(png(6001, 1))).status);
    assertEquals(0, requests.get()); assertEquals(0, stagedFiles());
  }

  @ParameterizedTest @ValueSource(ints = {401, 429, 503})
  void providerErrorsNeverPretendThePhotoIsSavedAndNeverLeakSecrets(int status) throws Exception {
    server.removeContext("/image/upload");
    server.createContext("/image/upload", e -> {
      byte[] response = "{\"error\":\"private-test-secret test-api-key\"}".getBytes();
      e.sendResponseHeaders(status, response.length); e.getResponseBody().write(response); e.close();
    });
    var error = assertThrows(Problem.class, () -> storage().store(png(12, 10)));
    assertEquals(503, error.status); assertFalse(error.getMessage().contains("private-test-secret"));
    assertFalse(error.getMessage().contains("test-api-key")); assertEquals(0, stagedFiles());
  }

  @Test void missingCredentialsDoNotSilentlySwitchToEphemeralStorage() {
    var s = new CloudinaryImageStorage(new LocalImageStorage(directory.toString()), "demo-cloud", "", "", CLOCK, HttpClient.newHttpClient(), endpoint);
    assertEquals(503, assertThrows(Problem.class, () -> s.store(png(12, 10))).status);
    assertEquals(0, requests.get()); assertEquals(0, stagedFiles());
  }

  @Test void malformedSuccessResponseCannotCreateAnUnusableOrForeignImageReference() {
    server.removeContext("/image/upload");
    server.createContext("/image/upload", e -> {
      byte[] body = "{\"public_id\":\"another-project/foreign\",\"resource_type\":\"image\",\"format\":\"jpg\"}".getBytes();
      e.sendResponseHeaders(200, body.length); e.getResponseBody().write(body); e.close();
    });
    assertEquals(503, assertThrows(Problem.class, () -> storage().store(png(12, 10))).status);
    assertEquals(0, stagedFiles());
  }

  private long stagedFiles() {
    if (!Files.exists(directory)) return 0;
    try (var files = Files.list(directory)) { return files.count(); }
    catch (IOException e) { throw new UncheckedIOException(e); }
  }
  private static MockMultipartFile png(int width, int height) throws IOException {
    var buffer = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png", buffer);
    return new MockMultipartFile("image", "photo.png", "image/png", buffer.toByteArray());
  }
  private static String sha256(String value) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
  }
  private record CapturedUpload(Map<String, String> fields, byte[] file) {}
  private static CapturedUpload capture(String contentType, byte[] body) {
    String boundary = contentType.substring(contentType.indexOf("boundary=") + 9);
    String raw = new String(body, StandardCharsets.ISO_8859_1);
    var fields = new HashMap<String, String>(); byte[] file = new byte[0];
    for (String part : raw.split(java.util.regex.Pattern.quote("--" + boundary))) {
      int endHeaders = part.indexOf("\r\n\r\n"); if (endHeaders < 0) continue;
      var key = java.util.regex.Pattern.compile("name=\"([^\"]+)\"").matcher(part.substring(0, endHeaders));
      if (!key.find()) continue;
      String content = part.substring(endHeaders + 4);
      if (content.endsWith("\r\n")) content = content.substring(0, content.length() - 2);
      if (key.group(1).equals("file")) file = content.getBytes(StandardCharsets.ISO_8859_1);
      else fields.put(key.group(1), content);
    }
    return new CapturedUpload(fields, file);
  }
}
