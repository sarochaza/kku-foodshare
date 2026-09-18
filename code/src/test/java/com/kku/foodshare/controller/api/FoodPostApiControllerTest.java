package com.kku.foodshare.controller.api;

import com.kku.foodshare.dto.response.MapFoodPostResponse;
import com.kku.foodshare.service.FoodPostService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FoodPostApiControllerTest {

    @Test
    void shouldReturnActiveFoodPostsFromService() {

        FoodPostService service =
                mock(FoodPostService.class);

        MapFoodPostResponse foodPost =
                createResponse();

        when(service.getActiveMapPosts())
                .thenReturn(List.of(foodPost));

        FoodPostApiController controller =
                new FoodPostApiController(service);

        List<MapFoodPostResponse> result =
                controller.getMapFoodPosts();

        assertEquals(1, result.size());
        assertEquals(
                "ข้าวกล่อง",
                result.get(0).getTitle()
        );

        verify(service).getActiveMapPosts();
    }

    @Test
    void shouldExposeMapRestEndpoint()
            throws NoSuchMethodException {

        RestController restController =
                FoodPostApiController.class
                        .getAnnotation(
                                RestController.class
                        );

        assertNotNull(restController);

        RequestMapping requestMapping =
                FoodPostApiController.class
                        .getAnnotation(
                                RequestMapping.class
                        );

        assertNotNull(requestMapping);

        assertArrayEquals(
                new String[]{
                        "/api/food-posts"
                },
                requestMapping.value()
        );

        Method method =
                FoodPostApiController.class
                        .getMethod(
                                "getMapFoodPosts"
                        );

        GetMapping getMapping =
                method.getAnnotation(
                        GetMapping.class
                );

        assertNotNull(getMapping);

        assertArrayEquals(
                new String[]{"/map"},
                getMapping.value()
        );
    }

    private MapFoodPostResponse createResponse() {

        return new MapFoodPostResponse(
                1L,
                "ข้าวกล่อง",
                "รับได้ที่หน้าอาคาร",
                "FOOD",
                10,
                "กล่อง",
                "อาคารวิทยวิภาส",
                new BigDecimal("16.4745000"),
                new BigDecimal("102.8237000"),
                LocalDateTime.of(
                        2026, 9, 18, 12, 0
                ),
                LocalDateTime.of(
                        2026, 9, 18, 15, 0
                ),
                "AVAILABLE",
                "🍱"
        );
    }
}