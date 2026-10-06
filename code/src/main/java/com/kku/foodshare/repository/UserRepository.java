package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// หมายถึง Repository นี้ใช้จัดการตาราง users
public interface UserRepository extends JpaRepository<User, Long> {
  // เอาไว้ตอน Login เช่น
  Optional<User> findByEmail(String email);

  // เอาไว้ตอน Register เพื่อเช็กว่า email ซ้ำไหม
  boolean existsByEmail(String email);

  Optional<User> findByEmailIgnoreCase(String email);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query(
      "select u from User u where lower(u.email) = lower(:email)")
  Optional<User> lockByEmail(
      @org.springframework.data.repository.query.Param("email") String email);
}
