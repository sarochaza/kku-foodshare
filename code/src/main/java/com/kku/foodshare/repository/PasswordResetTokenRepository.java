package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.PasswordResetToken;
import com.kku.foodshare.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  boolean existsByUserAndCreatedAtAfter(User user, java.time.Instant after);

  // ค้นหา Token จากค่า Hash
  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  // ลบ Token เก่าทั้งหมดของผู้ใช้ก่อนสร้าง Token ใหม่
  void deleteAllByUser(User user);
}
