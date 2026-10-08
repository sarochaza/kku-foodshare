package com.kku.foodshare.service.impl;

import com.kku.foodshare.service.EmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledEmailService implements EmailService {
  public void ensureAvailable() {
    throw new com.kku.foodshare.exception.Problem(503, "ระบบอีเมลยังไม่พร้อม กรุณาติดต่อผู้ดูแล");
  }

  public void sendPasswordResetEmail(String recipient, String resetUrl) {
    throw new IllegalStateException("ระบบอีเมลยังไม่พร้อม กรุณาติดต่อผู้ดูแล");
  }
}
