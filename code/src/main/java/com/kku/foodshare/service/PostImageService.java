package com.kku.foodshare.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kku.foodshare.domain.FoodPost;
import com.kku.foodshare.domain.PostImage;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.repository.PostImageRepository;

@Service
public class PostImageService {
    private final PostImageRepository postImageRepository;
    private final FoodPostRepository foodPostRepository;

    public PostImageService(
            PostImageRepository postImageRepository,
            FoodPostRepository foodPostRepository){
        this.postImageRepository = postImageRepository;
        this.foodPostRepository = foodPostRepository;
        }

    //ADD IMAGE
    public PostImage addImage(Long postId, PostImage postImage){
        if(postImage == null){
            throw new IllegalArgumentException("Post image must not be null");
        }
        if(postImage.getImageUrl() == null || postImage.getImageUrl().isBlank()){
            throw new IllegalArgumentException("Image URL is required");
        }
        FoodPost foodPost = foodPostRepository
            .findById(postId)
            .orElseThrow(() -> new RuntimeException("Food post not found: " + postId));
        postImage.setFoodPost(foodPost);
        return postImageRepository.save(postImage);
    }

    //FIND BY POSTID
    public List<PostImage> findByPostId(Long postId){
        if(!foodPostRepository.existsById(postId)){
            throw new RuntimeException("Food post not found: " + postId);
        }
        return postImageRepository.findByFoodPostPostIdOrderByDisplayOrderAsc(postId);
    }

    //FIND BY ID
    public PostImage findById(Long imageId){
        return postImageRepository.findById(imageId).orElseThrow(() -> new RuntimeException("Post image not found: " + imageId));
    }

    //UPDATE IMAGE
    public PostImage updateImage(Long imageId, PostImage updatedImage){
        if(updatedImage == null){
            throw new IllegalArgumentException("Post image must not be null");
        }
        if(updatedImage.getImageUrl() == null || updatedImage.getImageUrl().isBlank()){
            throw new IllegalArgumentException("Image URL is required");
        }
        PostImage existImage = findById(imageId);
        existImage.setImageUrl(updatedImage.getImageUrl());
        existImage.setDisplayOrder(updatedImage.getDisplayOrder());
        return postImageRepository.save(existImage);
    }

    //DELETE IMAGE
    public void deleteImage(Long imageId){
        PostImage postImage = findById(imageId);
        postImageRepository.delete(postImage);
    }
}
