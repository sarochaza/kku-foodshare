package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.User;

public interface MemberService {
  User require(String email);

  User admin(String email);
}
