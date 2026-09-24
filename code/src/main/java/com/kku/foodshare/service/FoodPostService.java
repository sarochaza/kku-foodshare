package com.kku.foodshare.service;

import com.kku.foodshare.domain.FoodPost;
import com.kku.foodshare.domain.FoodPostStatus;
import com.kku.foodshare.repository.FoodPostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodPostService{

    private final FoodPostRepository foodPostRepository;

    public FoodPostService(FoodPostRepository foodPostRepository) {
        this.foodPostRepository = foodPostRepository;
    }

    //CREATE
    public FoodPost create(FoodPost foodPost){
        validateFoodPost(foodPost);
        if (foodPost.getStatus() == null){
            foodPost.setStatus(FoodPostStatus.AVAILABLE);
        }
        if (foodPost.getEstimatedRemaining() == null){
            foodPost.setEstimatedRemaining(
                    foodPost.getInitialQuantity()
            );
        }
        return foodPostRepository.save(foodPost);
    }

    //READ ALL
    public List<FoodPost> findAll(){
        return foodPostRepository.findAll();
    }

    //READ BY ID
    public FoodPost findById(Long postId){
        return foodPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Food post not found" + postId));
    }

    //UPDATE
    public FoodPost update(Long postId, FoodPost updatedFoodPost){
        FoodPost existingFoodPost = findById(postId);
        validateFoodPost(updatedFoodPost);
        existingFoodPost.setUserId(updatedFoodPost.getUserId());
        existingFoodPost.setTitle(updatedFoodPost.getTitle());
        existingFoodPost.setDescription(updatedFoodPost.getDescription());
        existingFoodPost.setInitialQuantity(updatedFoodPost.getInitialQuantity());
        existingFoodPost.setEstimatedRemaining(updatedFoodPost.getEstimatedRemaining());
        existingFoodPost.setLocationName(updatedFoodPost.getLocationName());
        existingFoodPost.setLatitude(updatedFoodPost.getLatitude());
        existingFoodPost.setLongitude(updatedFoodPost.getLongitude());
        existingFoodPost.setStartTime(updatedFoodPost.getStartTime());
        existingFoodPost.setEndTime(updatedFoodPost.getEndTime());
        
        if (updatedFoodPost.getStatus() != null){
            existingFoodPost.setStatus(
                    updatedFoodPost.getStatus()
            );
        }
        return foodPostRepository.save(existingFoodPost);
    }

    //DELETE
    public void delete(Long postId){
        FoodPost foodPost = findById(postId);
        foodPostRepository.delete(foodPost);
    }

    //SEARCH BY TITLE
    public List<FoodPost> searchByTitle(String title){
        return foodPostRepository.findByTitleContainingIgnoreCase(title);
    }

    //FILTER BY STATUS
    public List<FoodPost> findByStatus(FoodPostStatus status){
        return foodPostRepository.findByStatus(status);
    }

    //VALIDATION
    private void validateFoodPost(FoodPost foodPost) {
        if(foodPost == null){
            throw new IllegalArgumentException("Food post must not be null");
        }

        if(foodPost.getUserId() == null){
            throw new IllegalArgumentException("User ID is required");
        }

        if(foodPost.getTitle() == null || foodPost.getTitle().isBlank()){
            throw new IllegalArgumentException("Title is required");
        }

        if(foodPost.getInitialQuantity() == null || foodPost.getInitialQuantity() <= 0){
            throw new IllegalArgumentException("Initial quantity must be greater than 0");
        }

        if(foodPost.getEstimatedRemaining() == null){
            foodPost.setEstimatedRemaining(foodPost.getInitialQuantity());
        }

        if(foodPost.getEstimatedRemaining() < 0){
            throw new IllegalArgumentException("Estimated remaining cannot be negative");
        }

        if(foodPost.getEstimatedRemaining() > foodPost.getInitialQuantity()){
            throw new IllegalArgumentException("Estimated remaining cannot be greater than initial quantity");
        }

        if(foodPost.getStartTime() != null && foodPost.getEndTime() != null && foodPost.getEndTime()
            .isBefore(foodPost.getStartTime())){
                throw new IllegalArgumentException("End time cannot be before start time");
            }
        }
}