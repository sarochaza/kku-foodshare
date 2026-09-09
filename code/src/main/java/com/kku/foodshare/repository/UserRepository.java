package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
//หมายถึง Repository นี้ใช้จัดการตาราง users
public interface UserRepository extends JpaRepository<User, Long> {
    //เอาไว้ตอน Login เช่น
    Optional<User> findByEmail(String email);
    //เอาไว้ตอน Register เพื่อเช็กว่า email ซ้ำไหม
    boolean existsByEmail(String email);
}