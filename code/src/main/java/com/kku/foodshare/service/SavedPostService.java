package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.dto.response.PostView;

public interface SavedPostService {
  void save(String email, long postId);
  void remove(String email, long postId);
  PageView<PostView> mine(String email, int page);
}
