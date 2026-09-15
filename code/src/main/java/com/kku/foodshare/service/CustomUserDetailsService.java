package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
/*CustomUserDetailsService มีหน้าที่ดึงข้อมูลผู้ใช้จากฐานข้อมูลด้วย 
Email เพื่อให้ Spring Security ใช้ตรวจสอบตัวตนตอน Login 
ส่วน Test ที่ทำคือ Unit Test เพื่อตรวจว่าการค้นหาและคืนข้อมูล User ทำงานถูกต้อง โดยใช้ Mock Repository แทนฐานข้อมูลจริงครับ */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "ไม่พบผู้ใช้ที่มี email: " + email
                        )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }
}