package com.kku.foodshare.service.event;

import com.kku.foodshare.domain.entity.User;

public record ActivityNotice(User user, User actor, String title, String message, String href) {
  public ActivityNotice(User user, String title, String message, String href) {
    this(user, null, title, message, href);
  }
}
