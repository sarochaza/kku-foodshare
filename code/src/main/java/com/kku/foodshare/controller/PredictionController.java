package com.kku.foodshare.controller;

import com.kku.foodshare.dto.PredictionResponse;
import com.kku.foodshare.service.PredictionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/{foodPostId}")
    public ResponseEntity<PredictionResponse> predict(
            @PathVariable Long foodPostId) {

        PredictionResponse response =
                predictionService.predict(foodPostId);

        return ResponseEntity.ok(response);
    }
}