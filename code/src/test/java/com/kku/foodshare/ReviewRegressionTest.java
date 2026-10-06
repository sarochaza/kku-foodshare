package com.kku.foodshare;

import static org.junit.jupiter.api.Assertions.*;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class ReviewRegressionTest {
  @Autowired UserRepository users;
  @Autowired PlatformTransactionManager transactions;
  @Autowired PasswordResetService resets;

  @Test
  void staleProfileCannotRestoreSuspendedAccount() {
    User u = new User();
    u.setEmail(java.util.UUID.randomUUID() + "@test.local");
    u.setPassword("unused");
    u.setDisplayName("before");
    users.saveAndFlush(u);
    User stale = users.findById(u.getId()).orElseThrow();
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            tx -> {
              User fresh = users.findById(u.getId()).orElseThrow();
              fresh.setActive(false);
              users.saveAndFlush(fresh);
            });
    stale.setDisplayName("changed");
    assertThrows(
        org.springframework.dao.OptimisticLockingFailureException.class,
        () -> users.saveAndFlush(stale));
    assertFalse(users.findById(u.getId()).orElseThrow().getActive());
  }

  @Test
  void disabledMailReturnsSameFailureForUnknownAndKnownEmail() {
    User u = new User();
    u.setEmail(java.util.UUID.randomUUID() + "@test.local");
    u.setPassword("unused");
    u.setDisplayName("member");
    users.saveAndFlush(u);
    var existing = assertThrows(RuntimeException.class, () -> resets.requestReset(u.getEmail()));
    var unknown =
        assertThrows(RuntimeException.class, () -> resets.requestReset("unknown@test.local"));
    assertEquals(existing.getClass(), unknown.getClass());
    assertEquals(existing.getMessage(), unknown.getMessage());
  }
}
