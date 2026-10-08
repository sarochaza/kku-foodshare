package com.kku.foodshare.service.impl;

import com.kku.foodshare.dto.response.MemberProfileResponse;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.MemberProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberProfileServiceImpl implements MemberProfileService {
  private final UserRepository users;

  public MemberProfileServiceImpl(UserRepository users) { this.users = users; }

  @Override
  public MemberProfileResponse get(long id) {
    var user = users.findById(id).orElseThrow(Problem::missing);
    if (!Boolean.TRUE.equals(user.getActive())) throw Problem.missing();
    return new MemberProfileResponse(user.getId(), user.getDisplayName());
  }
}
