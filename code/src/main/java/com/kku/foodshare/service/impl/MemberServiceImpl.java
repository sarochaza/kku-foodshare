package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.domain.enums.UserRole;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.MemberService;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {
  private final UserRepository users;

  public MemberServiceImpl(UserRepository users) {
    this.users = users;
  }

  public User require(String email) {
    if (email == null) throw new Problem(401, "กรุณาเข้าสู่ระบบ");
    User u = users.findByEmailIgnoreCase(email.trim()).orElseThrow(Problem::forbidden);
    if (!Boolean.TRUE.equals(u.getActive()))
      throw new Problem(403, "บัญชีนี้ถูกระงับ กรุณาติดต่อผู้ดูแล");
    return u;
  }

  public User admin(String email) {
    User u = require(email);
    if (u.getRole() != UserRole.ADMIN) throw Problem.forbidden();
    return u;
  }
}
