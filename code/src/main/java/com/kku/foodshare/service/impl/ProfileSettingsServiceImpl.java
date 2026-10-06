package com.kku.foodshare.service.impl;

import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileSettingsServiceImpl implements ProfileSettingsService {
  private final MemberService members;

  public ProfileSettingsServiceImpl(MemberService members) {
    this.members = members;
  }

  @Transactional
  public void rename(String email, String name) {
    if (name == null || name.isBlank() || name.length() > 80)
      throw new Problem(400, "กรุณาระบุชื่อ 1–80 ตัวอักษร");
    members.require(email).setDisplayName(name.trim());
  }
}
