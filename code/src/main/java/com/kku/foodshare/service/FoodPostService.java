package com.kku.foodshare.service;

import com.kku.foodshare.domain.FoodPost;
import com.kku.foodshare.repository.FoodPostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodPostService {

    private final FoodPostRepository foodPostRepository;

    public FoodPostService(FoodPostRepository foodPostRepository) {
        this.foodPostRepository = foodPostRepository;
    }

    //สร้าง Food Post
    public FoodPost create(FoodPost foodPost) {
        return foodPostRepository.save(foodPost);
    }

    //ค้นหา Food Post ทั้งหมด
    public List<FoodPost> findAll() {
        return foodPostRepository.findAll();
    }

    //ค้นหา Food Post ตาม ID
    public FoodPost findById(Long postId) {
        return foodPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Food post not found"));
    }
}