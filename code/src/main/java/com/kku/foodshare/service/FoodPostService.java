package com.kku.foodshare.service;

import java.util.List;

import com.kku.foodshare.dto.response.MapFoodPostResponse;

public interface FoodPostService {

    List<MapFoodPostResponse>
            getActiveMapPosts();
}
