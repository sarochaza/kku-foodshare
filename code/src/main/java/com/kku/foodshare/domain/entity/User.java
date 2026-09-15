package com.kku.foodshare.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //คือ Primary Key และ PostgreSQL จะสร้างเลข id เพิ่มให้อัตโนมัติ

    @Column(nullable = false, unique = true)
    private String email;
    //email ห้ามว่าง และห้ามซ้ำ เพราะใช้ Login

    @Column(nullable = false)
    private String password;
    //ตอนนี้เก็บ field ไว้ก่อน แต่ตอนทำ Register จริง เราจะเข้ารหัสด้วย BCrypt ไม่เก็บ password ตรง ๆ
    
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}