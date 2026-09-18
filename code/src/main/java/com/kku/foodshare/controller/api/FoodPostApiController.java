package com.kku.foodshare.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kku.foodshare.dto.response.MapFoodPostResponse;
import com.kku.foodshare.service.FoodPostService;

@RestController
@RequestMapping("/api/food-posts")
public class FoodPostApiController {

    private final FoodPostService service;

    public FoodPostApiController(
            FoodPostService service
    ) {
        this.service = service;
    }

    @GetMapping("/map")
    public List<MapFoodPostResponse>
            getMapFoodPosts() {

        return service.getActiveMapPosts();
    }
}