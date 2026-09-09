package com.kku.foodshare.service.impl;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.request.RegisterRequest;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.UserService;
import org.springframework.stereotype.Service;
//บอก Spring ว่า class นี้คือ Service Layer
@Service
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    //คือ Constructor Injection
    private final UserRepository userRepository;

    public UserServiceImpl(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder) {

    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
}

    @Override
    public User register(RegisterRequest request) {
        //คือเช็กก่อนว่า email ซ้ำหรือไม่
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        //ไม่ซ้ำ สร้าง
        User user = new User();

        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());

        return userRepository.save(user);
    }
}