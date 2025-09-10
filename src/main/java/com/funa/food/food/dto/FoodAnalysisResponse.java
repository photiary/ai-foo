package com.funa.food.food.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodAnalysisResponse {
    private Long id; // AnalysisFood id (DB identifier)
    private List<FoodItem> foods;
    private String userStatus;
    private String suitability; // overall suitability considering user status
    private String suggestion;  // better meal suggestion

    private String analysisMode; // IMG_ONLY | IMG_SUGG
    private OffsetDateTime createdAt; // 데이터 생성일

    // Added: Token usage information
    private UsageInfo usage;
    // Added: Billing information
    private BillingInfo billing;

    private String imageSize;

    private String imageFileName;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsageInfo {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
        private Integer cachedTokens;
        private String modelName;
        private Long requestDurationMs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillingInfo {
        private BigDecimal inputCost;   // USD
        private BigDecimal cachedInputCost; // USD (cached input)
        private BigDecimal outputCost;  // USD
        private BigDecimal totalCost;   // USD
        private String currency;        // e.g., USD
    }
}
