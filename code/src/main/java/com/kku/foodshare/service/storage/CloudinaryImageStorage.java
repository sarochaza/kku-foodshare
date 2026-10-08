package com.kku.foodshare.service.storage;

import com.kku.foodshare.exception.Problem;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary
@ConditionalOnProperty(name = "app.images.provider", havingValue = "cloudinary")
public class CloudinaryImageStorage implements ImageStorage {
  private static final String CLOUD_NAME = "[A-Za-z0-9_-]{1,80}";
  private static final String CLOUD_FILE = "cld-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.jpg";
  private final LocalImageStorage local;
  private final String cloudName;
  private final String apiKey;
  private final String apiSecret;
  private final Clock clock;
  private final HttpClient client;
  private final URI endpoint;

  @Autowired
  public CloudinaryImageStorage(LocalImageStorage local,
      @Value("${app.images.cloudinary.cloud-name:}") String cloudName,
      @Value("${app.images.cloudinary.api-key:}") String apiKey,
      @Value("${app.images.cloudinary.api-secret:}") String apiSecret, Clock clock) {
    this(local, cloudName, apiKey, apiSecret, clock,
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
        URI.create("https://api.cloudinary.com/v1_1/"
            + (cloudName.trim().matches(CLOUD_NAME) ? cloudName.trim() : "unconfigured") + "/image/"));
  }

  CloudinaryImageStorage(LocalImageStorage local, String cloudName, String apiKey,
      String apiSecret, Clock clock, HttpClient client, URI endpoint) {
    this.local = local; this.cloudName = cloudName.trim(); this.apiKey = apiKey.trim();
    this.apiSecret = apiSecret.trim(); this.clock = clock; this.client = client; this.endpoint = endpoint;
  }

  @Override
  public String store(MultipartFile file) {
    available();
    // Reuse all existing file validation, decoding, metadata removal and 1400px resizing.
    String staged = local.store(file);
    String name = "cld-" + UUID.randomUUID() + ".jpg";
    try {
      var signed = new TreeMap<String, String>();
      signed.put("overwrite", "false"); signed.put("public_id", publicId(name));
      signed.put("timestamp", Long.toString(clock.instant().getEpochSecond()));
      var fields = authenticated(signed);
      String boundary = "foodshare-" + UUID.randomUUID();
      byte[] jpeg;
      try (var input = local.load(staged).getInputStream()) { jpeg = input.readAllBytes(); }
      var request = HttpRequest.newBuilder(endpoint.resolve("upload")).timeout(Duration.ofSeconds(15))
          .header("Content-Type", "multipart/form-data; boundary=" + boundary)
          .POST(HttpRequest.BodyPublishers.ofByteArray(multipart(fields, jpeg, boundary))).build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() < 200 || response.statusCode() >= 300) throw unavailable();
      var json = new tools.jackson.databind.ObjectMapper().readTree(response.body());
      if (!publicId(name).equals(json.path("public_id").asString())
          || !"image".equals(json.path("resource_type").asString())
          || !"jpg".equals(json.path("format").asString())) {
        remove(name);
        throw unavailable();
      }
      return name;
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt(); throw unavailable();
    } catch (IOException | tools.jackson.core.JacksonException failure) {
      throw unavailable();
    } finally { local.remove(staged); }
  }

  @Override
  public URI publicUrl(String name) {
    if (!isCloudImage(name)) return null;
    if (!cloudName.matches(CLOUD_NAME)) throw Problem.missing();
    return URI.create("https://res.cloudinary.com/" + cloudName + "/image/upload/" + publicId(name) + ".jpg");
  }

  @Override
  public Resource load(String name) {
    URI url = publicUrl(name);
    if (url == null) return local.load(name);
    try { return new UrlResource(url); }
    catch (MalformedURLException failure) { throw Problem.missing(); }
  }

  @Override
  public void remove(String name) {
    if (!isCloudImage(name)) { local.remove(name); return; }
    try {
      available();
      var signed = new TreeMap<String, String>();
      signed.put("invalidate", "true"); signed.put("public_id", publicId(name));
      signed.put("timestamp", Long.toString(clock.instant().getEpochSecond()));
      String body = authenticated(signed).entrySet().stream()
          .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
              + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
      var request = HttpRequest.newBuilder(endpoint.resolve("destroy")).timeout(Duration.ofSeconds(10))
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(HttpRequest.BodyPublishers.ofString(body)).build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() < 200 || response.statusCode() >= 300) throw unavailable();
      String result = new tools.jackson.databind.ObjectMapper().readTree(response.body()).path("result").asString();
      if (!Set.of("ok", "not found").contains(result)) throw unavailable();
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt(); logRemovalFailure(name);
    } catch (IOException | RuntimeException failure) { logRemovalFailure(name); }
  }

  private void available() {
    if (!cloudName.matches(CLOUD_NAME) || apiKey.isBlank() || apiSecret.isBlank())
      throw new Problem(503, "ระบบเก็บรูปยังไม่พร้อม กรุณาติดต่อผู้ดูแลเพื่อตั้งค่าการเก็บรูป");
  }
  private Problem unavailable() { return new Problem(503, "อัปโหลดรูปไม่สำเร็จ ระบบเก็บรูปไม่พร้อม กรุณาลองใหม่อีกครั้ง"); }
  private boolean isCloudImage(String name) { return name != null && name.matches(CLOUD_FILE); }
  private String publicId(String name) { return "kku-foodshare/" + name.substring(0, name.length() - 4); }
  private void logRemovalFailure(String name) {
    LoggerFactory.getLogger(getClass()).warn("Unable to remove cloud image {}; check image storage configuration", name);
  }
  private Map<String, String> authenticated(SortedMap<String, String> signed) {
    String canonical = signed.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
    var fields = new LinkedHashMap<String, String>(signed);
    try {
      fields.put("signature", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
          .digest((canonical + apiSecret).getBytes(StandardCharsets.UTF_8))));
    } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException("SHA-256 unavailable", impossible); }
    fields.put("api_key", apiKey); return fields;
  }
  private byte[] multipart(Map<String, String> fields, byte[] jpeg, String boundary) throws IOException {
    var output = new ByteArrayOutputStream();
    for (var field : fields.entrySet()) {
      output.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + field.getKey()
          + "\"\r\n\r\n" + field.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
    }
    output.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"image.jpg\""
        + "\r\nContent-Type: image/jpeg\r\n\r\n").getBytes(StandardCharsets.UTF_8));
    output.write(jpeg); output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
    return output.toByteArray();
  }
}
