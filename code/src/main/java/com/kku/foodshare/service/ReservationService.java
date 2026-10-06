package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.*;
import java.util.List;

public interface ReservationService {
  ReservationView reserve(String email, long postId, int quantity, String key);

  ReservationView mineForPost(String email, long postId);

  ReservationView get(String email, long id);

  ReservationView changeQuantity(String email, long id, int quantity);

  void cancel(String email, long id);

  ReservationView collect(String email, long id, String code);

  PageView<ReservationView> mine(String email, int page);

  List<ReservationView> forPost(String email, long postId);

  void expire(long postId);
}
