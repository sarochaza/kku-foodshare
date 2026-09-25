package com.kku.foodshare.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kku.foodshare.domain.FoodPost;
import com.kku.foodshare.domain.PostImage;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.repository.PostImageRepository;

@ExtendWith(MockitoExtension.class)
public class PostImageServiceTest {
    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private FoodPostRepository foodPostRepository;

    @InjectMocks
    private PostImageService postImageService;

    private FoodPost foodPost;
    private PostImage postImage;

    @BeforeEach
    void setUp(){
        foodPost = new FoodPost();
        foodPost.setPostId(1L);
        foodPost.setUserId(1L);
        foodPost.setTitle("ข้าวกล่อง");
        foodPost.setInitialQuantity(10);
        foodPost.setEstimatedRemaining(10);
        
        postImage = new PostImage();
        postImage.setImageId(1L);
        postImage.setImageUrl("https://i.pinimg.com/1200x/6f/0b/a1/6f0ba1f70f2e9b6bedfbea19d8ff099a.jpg");
        postImage.setDisplayOrder(1);
    }

    @Test
    void shouldAddImageToFoodPost(){
        when(foodPostRepository.findById(1L))
            .thenReturn(Optional.of(foodPost));

        when(postImageRepository.save(any(PostImage.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        PostImage result = postImageService.addImage(1L, postImage);
        assertNotNull(result);
        assertEquals("https://i.pinimg.com/1200x/6f/0b/a1/6f0ba1f70f2e9b6bedfbea19d8ff099a.jpg", result.getImageUrl());
        assertEquals(foodPost, result.getFoodPost());
        verify(foodPostRepository).findById(1L);
        verify(postImageRepository).save(postImage);
    }

    @Test
    void shouldFindImageByPostId(){
        when(foodPostRepository.existsById(1L))
            .thenReturn(true);

        when(postImageRepository.findByFoodPostPostIdOrderByDisplayOrderAsc(1L))
            .thenReturn(List.of(postImage));

        List<PostImage> result = postImageService.findByPostId(1L);
        assertEquals(1, result.size());
        assertEquals("https://i.pinimg.com/1200x/6f/0b/a1/6f0ba1f70f2e9b6bedfbea19d8ff099a.jpg",
                result.get(0).getImageUrl());
    }

    @Test
    void shouldFindImageById(){
        when(postImageRepository.findById(1L))
            .thenReturn(Optional.of(postImage));
        PostImage result = postImageService.findById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getImageId());
    }

    @Test
    void shouldThrownExceptionWhenImageNotFound(){
        when(postImageRepository.findById(99L))
            .thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> postImageService.findById(99L));
    }

    @Test
    void shouldUpdateImage(){
        PostImage updatedImage = new PostImage();
        updatedImage.setImageUrl("https://i.pinimg.com/736x/9f/51/46/9f5146ca1e0b8e1da64113caa08cbf7d.jpg");
        updatedImage.setDisplayOrder(2);
        when(postImageRepository.findById(1L))
            .thenReturn(Optional.of(postImage));
        when(postImageRepository.save(any(PostImage.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        PostImage result = postImageService.updateImage(1L, updatedImage);
        assertEquals("https://i.pinimg.com/736x/9f/51/46/9f5146ca1e0b8e1da64113caa08cbf7d.jpg", 
            result.getImageUrl());
        assertEquals(2, result.getDisplayOrder());
        verify(postImageRepository).save(postImage);
    }

    @Test
    void shouldDeleteImage(){
        when(postImageRepository.findById(1L))
            .thenReturn(Optional.of(postImage));
        postImageService.deleteImage(1L);
        verify(postImageRepository).delete(postImage);
    }

    @Test
    void shouldRejectEmptyImageUrl(){
        PostImage invalidImage = new PostImage();
        invalidImage.setImageUrl("");
        assertThrows(IllegalArgumentException.class, 
            () -> postImageService.addImage(1L, invalidImage));
        verify(postImageRepository, never()).save(any(PostImage.class));
    }

    @Test
    void shouldRejectImageWhenFoodPostNotFound(){
        when(foodPostRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> postImageService.addImage(99L, postImage)
        );
        verify(postImageRepository, never()).save(any(PostImage.class));
    }
}
