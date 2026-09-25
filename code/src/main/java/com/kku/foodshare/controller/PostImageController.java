package com.kku.foodshare.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kku.foodshare.domain.PostImage;
import com.kku.foodshare.service.PostImageService;

@RestController
@RequestMapping("/api/food-posts/{postId}/images")
public class PostImageController {
    private final PostImageService postImageService;
    
    public PostImageController(PostImageService postImageService){
        this.postImageService = postImageService;
    }

    //ADD IMAGE
    @PostMapping
    public ResponseEntity<PostImage> addImage(
        @PathVariable Long postId,
        @RequestBody PostImage postImage){
        PostImage creatImage = postImageService.addImage(postId, postImage);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creatImage);
    }

    //READ
    @GetMapping
    public ResponseEntity<List<PostImage>> getImages(@PathVariable Long postId){
        List<PostImage> images = postImageService.findByPostId(postId);
        return ResponseEntity.ok(images);
    }

    //READ BY ID
    @GetMapping("/{imageId}")
    public ResponseEntity<PostImage> getImageId(
        @PathVariable Long postId,
        @PathVariable Long imageId){
        PostImage image = postImageService.findById(imageId);
        return ResponseEntity.ok(image);
    }

    //UPDATE IMAGE
    @PutMapping("/{imageId}")
    public ResponseEntity<PostImage> updateImage(
        @PathVariable Long postId,
        @PathVariable Long imageId,
        @RequestBody PostImage postImage){
            PostImage updatedImage = postImageService.updateImage(imageId, postImage);
            return ResponseEntity.ok(updatedImage);
        }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<PostImage> deleteImage(
        @PathVariable Long postId,
        @PathVariable Long imageId){
            postImageService.deleteImage(imageId);
            return ResponseEntity.noContent().build();
        }
}
