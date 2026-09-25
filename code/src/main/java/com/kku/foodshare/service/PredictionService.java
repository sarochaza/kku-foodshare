package com.kku.foodshare.service;

import com.kku.foodshare.dto.PredictionResponse;

public interface PredictionService {

    PredictionResponse predict(Long foodPostId);
}