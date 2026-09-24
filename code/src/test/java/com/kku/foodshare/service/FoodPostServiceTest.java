package com.kku.foodshare.service;

import com.kku.foodshare.domain.FoodPost;
import com.kku.foodshare.domain.FoodPostStatus;
import com.kku.foodshare.repository.FoodPostRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FoodPostServiceTest {
    @Mock
    private FoodPostRepository foodPostRepository;

    @InjectMocks
    private FoodPostService foodPostService;

    //CREATE
    @Test
    void shouldCreateFoodPost(){
        FoodPost foodPost = createValidFoodPost();
        
        when(foodPostRepository.save(foodPost)).thenReturn(foodPost);
        FoodPost result = foodPostService.create(foodPost);
        
        assertEquals(foodPost, result);
        verify(foodPostRepository).save(foodPost);
    }

    //FIND ALL
    @Test
    void shouldFindAllFoodPosts(){
        FoodPost foodPost1 = createValidFoodPost();
        FoodPost foodPost2 = createValidFoodPost();
        
        when(foodPostRepository.findAll()).thenReturn(List.of(foodPost1, foodPost2));
        List<FoodPost> result = foodPostService.findAll();
        
        assertEquals(2, result.size());
        assertEquals(foodPost1, result.get(0));
        assertEquals(foodPost2, result.get(1));
    }

    // FIND BY ID
    @Test
    void shouldFindFoodPostById(){
        FoodPost foodPost = createValidFoodPost();
        
        when(foodPostRepository.findById(1L)).thenReturn(Optional.of(foodPost));
        FoodPost result = foodPostService.findById(1L);
        
        assertEquals(foodPost,result);
    }

    //FIND BY ID - NOT FOUND
    @Test
    void shouldThrowExceptionWhenFoodPostNotFound(){
        when(foodPostRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> foodPostService.findById(999L));
    }

    // UPDATE
    @Test
    void shouldUpdateFoodPost(){
        FoodPost existingFoodPost = createValidFoodPost();
        FoodPost updatedFoodPost = createValidFoodPost();
        updatedFoodPost.setTitle("ข้าวกะเพราไก่");
        
        when(foodPostRepository.findById(1L)).thenReturn(Optional.of(existingFoodPost));
        when(foodPostRepository.save(existingFoodPost)).thenReturn(existingFoodPost);
        FoodPost result = foodPostService.update(1L, updatedFoodPost);
        
        assertEquals("ข้าวกะเพราไก่",result.getTitle());
        verify(foodPostRepository).save(existingFoodPost);
    }

    // DELETE
    @Test
    void shouldDeleteFoodPost(){
        FoodPost foodPost = createValidFoodPost();
        
        when(foodPostRepository.findById(1L)).thenReturn(Optional.of(foodPost));
        foodPostService.delete(1L);
        
        verify(foodPostRepository).delete(foodPost);
    }

    // SEARCH
    @Test
    void shouldSearchFoodPostByTitle(){
        FoodPost foodPost = createValidFoodPost();
        
        when(foodPostRepository.findByTitleContainingIgnoreCase("กะเพรา")).thenReturn(List.of(foodPost));
        List<FoodPost> result = foodPostService.searchByTitle("กะเพรา");
        
        assertEquals(1, result.size());
    }

    // FILTER
    @Test
    void shouldFindFoodPostByStatus(){
        FoodPost foodPost = createValidFoodPost();
        
        when(foodPostRepository.findByStatus(FoodPostStatus.AVAILABLE)).thenReturn(List.of(foodPost));
        List<FoodPost> result = foodPostService.findByStatus(FoodPostStatus.AVAILABLE);
        
        assertEquals(1, result.size());
    }

    // VALIDATION
    @Test
    void shouldRejectEmptyTitle(){
        FoodPost foodPost = createValidFoodPost();
        foodPost.setTitle("");
        assertThrows(IllegalArgumentException.class, () -> foodPostService.create(foodPost));
    }

    @Test
    void shouldRejectInvalidQuantity(){
        FoodPost foodPost = createValidFoodPost();
        foodPost.setInitialQuantity(0);
        assertThrows(IllegalArgumentException.class, () -> foodPostService.create(foodPost));
    }

    // TEST DATA
    private FoodPost createValidFoodPost() {
        FoodPost foodPost = new FoodPost();
        foodPost.setUserId(1L);
        foodPost.setTitle("ข้าวกะเพรา");
        foodPost.setDescription("เหลือจากกิจกรรมชมรม");
        foodPost.setInitialQuantity(10);
        foodPost.setEstimatedRemaining(10);
        foodPost.setLocationName("อาคารเรียนรวม");
        foodPost.setStatus(FoodPostStatus.AVAILABLE);
        return foodPost;
    }
}