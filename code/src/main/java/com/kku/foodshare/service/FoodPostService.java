package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.MapFoodPostResponse;
import java.util.List;

public interface FoodPostService {

  List<MapFoodPostResponse> getActiveMapPosts();
}
