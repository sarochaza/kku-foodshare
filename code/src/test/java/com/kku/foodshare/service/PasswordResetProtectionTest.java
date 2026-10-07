package com.kku.foodshare.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.service.impl.PasswordResetServiceImpl;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PasswordResetProtectionTest {
  @Test
  void cooldownDoesNotInvalidatePreviousTokenOrSendAnotherEmail() {
    var users = mock(UserRepository.class);
    var tokens = mock(PasswordResetTokenRepository.class);
    var mail = mock(EmailService.class);
    var clock = Clock.fixed(Instant.parse("2026-10-05T10:00:00Z"), ZoneOffset.UTC);
    var u = new User();
    u.setEmail("member@test.local");
    when(users.lockByEmail(u.getEmail())).thenReturn(Optional.of(u));
    when(tokens.existsByUserAndCreatedAtAfter(u, clock.instant().minusSeconds(300)))
        .thenReturn(true);
    var s =
        new PasswordResetServiceImpl(
            users,
            tokens,
            mock(org.springframework.security.crypto.password.PasswordEncoder.class),
            mail,
            clock,
            "https://example.org");
    s.requestReset(u.getEmail());
    verify(tokens, never()).deleteAllByUser(any());
    verify(mail, never()).sendPasswordResetEmail(any(), any());
  }

  @Test
  void publicRequestsAreLimitedPerAddress() {
    var limiter = new RequestRateLimiter(Clock.systemUTC());
    for (int i = 0; i < 5; i++) limiter.passwordReset("192.0.2.1");
    assertEquals(429, assertThrows(Problem.class, () -> limiter.passwordReset("192.0.2.1")).status);
    assertDoesNotThrow(() -> limiter.passwordReset("192.0.2.2"));
  }

  @Test
  void smtpFailureDoesNotRevealKnownEmail() {
    var users = mock(UserRepository.class);
    var tokens = mock(PasswordResetTokenRepository.class);
    var sender = mock(org.springframework.mail.javamail.JavaMailSender.class);
    when(sender.createMimeMessage())
        .thenReturn(new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null));
    doThrow(new org.springframework.mail.MailSendException("test outage"))
        .when(sender)
        .send(any(jakarta.mail.internet.MimeMessage.class));
    var mail = new com.kku.foodshare.service.impl.SmtpEmailService(sender, "noreply@example.org");
    var member = new User();
    member.setEmail("known@example.org");
    when(users.lockByEmail("known@example.org")).thenReturn(Optional.of(member));
    when(users.lockByEmail("unknown@example.org")).thenReturn(Optional.empty());
    var service =
        new PasswordResetServiceImpl(
            users,
            tokens,
            mock(org.springframework.security.crypto.password.PasswordEncoder.class),
            mail,
            Clock.systemUTC(),
            "https://example.org");
    assertDoesNotThrow(() -> service.requestReset("known@example.org"));
    assertDoesNotThrow(() -> service.requestReset("unknown@example.org"));
    verify(tokens).delete(any(com.kku.foodshare.domain.entity.PasswordResetToken.class));
  }

  @Test
  void deliveryFailureAllowsTheNextRequestToRetryWithoutLeakingToken() {
    var users = mock(UserRepository.class);
    var tokens = mock(PasswordResetTokenRepository.class);
    var mail = mock(EmailService.class);
    var member = new User(); member.setEmail("retry@test.local");
    when(users.lockByEmail(member.getEmail())).thenReturn(Optional.of(member));
    doThrow(new org.springframework.mail.MailSendException("outage"))
        .when(mail).sendPasswordResetEmail(any(), any());
    var service = new PasswordResetServiceImpl(users, tokens,
        mock(org.springframework.security.crypto.password.PasswordEncoder.class),
        mail, Clock.systemUTC(), "http://localhost:8081");
    service.requestReset(member.getEmail());
    service.requestReset(member.getEmail());
    verify(mail, times(2)).sendPasswordResetEmail(eq(member.getEmail()), any());
    verify(tokens, times(2)).delete(any(com.kku.foodshare.domain.entity.PasswordResetToken.class));
  }

  @Test
  void expiredUsedOrSuspendedAccountTokensCannotResetPassword() {
    var tokens = mock(PasswordResetTokenRepository.class);
    var clock = Clock.fixed(Instant.parse("2026-10-07T08:00:00Z"), ZoneOffset.UTC);
    var member = new User();
    var token = new com.kku.foodshare.domain.entity.PasswordResetToken();
    token.setUser(member); token.setExpiresAt(clock.instant().plusSeconds(900));
    when(tokens.findByTokenHash(any())).thenReturn(Optional.of(token));
    var service = new PasswordResetServiceImpl(mock(UserRepository.class), tokens,
        mock(org.springframework.security.crypto.password.PasswordEncoder.class),
        mock(EmailService.class), clock, "http://localhost:8081");
    assertTrue(service.isTokenValid("raw-token"));
    token.setExpiresAt(clock.instant());
    assertFalse(service.isTokenValid("raw-token"));
    assertThrows(IllegalArgumentException.class, () -> service.resetPassword("raw-token", "NewPassword123!"));
    token.setExpiresAt(clock.instant().plusSeconds(900)); token.setUsedAt(clock.instant());
    assertFalse(service.isTokenValid("raw-token"));
    token.setUsedAt(null); member.setActive(false);
    assertFalse(service.isTokenValid("raw-token"));
    assertThrows(IllegalArgumentException.class, () -> service.resetPassword("raw-token", "NewPassword123!"));
    assertFalse(service.isTokenValid(null));
    verify(tokens, never()).save(any());
  }
}
