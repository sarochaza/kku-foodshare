package com.kku.foodshare.service;

import com.kku.foodshare.dto.request.FoodPostRequest;
import com.kku.foodshare.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

public interface FoodCatalogService {
  PostView create(String email, FoodPostRequest input);

  PostView update(String email, long id, FoodPostRequest input);

  PostView get(long id, String email);

  void delete(String email, long id);

  PostView image(String email, long id, MultipartFile file);

  PageView<PostView> search(
      String text,
      String category,
      String sort,
      Double lat,
      Double lng,
      boolean now,
      int page,
      int size,
      String email);

  PageView<PostView> mine(String email, int page);

  java.util.Map<String, Long> stats();
}
