package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.request.RegisterRequest;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();

        user.setEmail(request.getEmail());
        //ผ่าน BCrypt ก่อน แล้วกลายเป็นค่าประมาณ
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());

        return userRepository.save(user);
    }
}