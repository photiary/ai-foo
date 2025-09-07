package com.funa.food.food.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodAnalysisResponse {
    private List<FoodItem> foods;
    private String suitability; // overall suitability considering user status
    private String suggestion;  // better meal suggestion

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodItem {
        private String name;
        private double protein;
        private double carbs;
        private double fat;
        private double calories;
        private Position position;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Position {
        private int x;
        private int y;
    }
}
