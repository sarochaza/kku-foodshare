package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.MemberProfileResponse;

public interface MemberProfileService {
  MemberProfileResponse get(long id);
}
