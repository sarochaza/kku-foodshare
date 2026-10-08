package com.kku.foodshare.service.impl;

import com.kku.foodshare.service.PickupCodeService;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PickupCodeServiceImpl implements PickupCodeService {
  private final SecretKeySpec key;
  private final PasswordEncoder encoder;
  private final SecureRandom random = new SecureRandom();

  public PickupCodeServiceImpl(@Value("${app.secret}") String secret, PasswordEncoder encoder)
      throws GeneralSecurityException {
    if (secret.length() < 32)
      throw new IllegalStateException(
          "Set APP_SECRET to at least 32 random characters before startup");
    key =
        new SecretKeySpec(
            MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)),
            "AES");
    this.encoder = encoder;
  }

  public String generate() {
    return "%06d".formatted(random.nextInt(1000000));
  }

  public String hash(String code) {
    return encoder.encode(code);
  }

  public boolean matches(String code, String hash) {
    return encoder.matches(code, hash);
  }

  public String encrypt(String code) {
    try {
      byte[] iv = new byte[12];
      random.nextBytes(iv);
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
      byte[] bytes = c.doFinal(code.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder()
          .encodeToString(ByteBuffer.allocate(iv.length + bytes.length).put(iv).put(bytes).array());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Code encryption failed", e);
    }
  }

  public String decrypt(String encrypted) {
    try {
      ByteBuffer b = ByteBuffer.wrap(Base64.getDecoder().decode(encrypted));
      byte[] iv = new byte[12];
      b.get(iv);
      byte[] bytes = new byte[b.remaining()];
      b.get(bytes);
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
      return new String(c.doFinal(bytes), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Code decryption failed: verify APP_SECRET", e);
    }
  }
}
