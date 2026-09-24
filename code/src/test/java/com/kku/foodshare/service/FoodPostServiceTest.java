package com.kku.foodshare.service;

import com.kku.foodshare.domain.FoodPost;
import com.kku.foodshare.repository.FoodPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FoodPostServiceTest {
    @Mock
    private FoodPostRepository foodPostRepository;

    @InjectMocks
    private FoodPostService foodPostService;

    @Test
    void shouldFindFoodPostById(){
        FoodPost foodPost = new FoodPost();
        when(foodPostRepository.findById(1L)).thenReturn(java.util.Optional.of(foodPost));
        FoodPost result = foodPostService.findById(1L);
        assertEquals(foodPost, result);
    }
}