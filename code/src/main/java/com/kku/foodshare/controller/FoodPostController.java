package com.kku.foodshare.controller;

import com.kku.foodshare.domain.FoodPost;
import com.kku.foodshare.domain.FoodPostStatus;
import com.kku.foodshare.service.FoodPostService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-posts")
public class FoodPostController {
    private final FoodPostService foodPostService;

    public FoodPostController(FoodPostService foodPostService){
        this.foodPostService = foodPostService;
    }

    //CREATE
    @PostMapping
    public ResponseEntity<FoodPost> create(@RequestBody FoodPost foodPost){
        FoodPost createdFoodPost = foodPostService.create(foodPost);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFoodPost);
    }
    
    //READ ALL
    @GetMapping
    public ResponseEntity<List<FoodPost>> getAll(){
        List<FoodPost> foodPosts = foodPostService.findAll();
        return ResponseEntity.ok(foodPosts);
    }

    //READ BY ID
    @GetMapping("/{postId}")
    public ResponseEntity<FoodPost> getById(@PathVariable Long postId){
        FoodPost foodPost = foodPostService.findById(postId);
        return ResponseEntity.ok(foodPost);
    }

    //UPDATE
    @PutMapping("/{postId}")
    public ResponseEntity<FoodPost> update(
            @PathVariable Long postId,
            @RequestBody FoodPost foodPost){
        FoodPost updatedFoodPost = foodPostService.update(postId, foodPost);
        return ResponseEntity.ok(updatedFoodPost);
    }

    //DELETE
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId){
        foodPostService.delete(postId);
        return ResponseEntity.noContent().build();
    }

    //SEARCH
    @GetMapping("/search")
    public ResponseEntity<List<FoodPost>> search(@RequestParam String title){
        List<FoodPost> foodPosts = foodPostService.searchByTitle(title);
        return ResponseEntity.ok(foodPosts);
    }

    //FILTER BY STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FoodPost>> findByStatus(@PathVariable FoodPostStatus status){
        List<FoodPost> foodPosts = foodPostService.findByStatus(status);
        return ResponseEntity.ok(foodPosts);
    }
}
