package com.funa.food.food;

import java.util.Locale;

public enum AnalysisMode {
    IMAGE_ONLY,
    IMAGE_WITH_SUGGESTION;

    /**
     * Parse analysis mode from request parameter.
     * Supported values (case-insensitive):
     * - IMG_ONLY, IMAGE_ONLY
     * - IMG_SUGG, IMAGE_WITH_SUGGESTION
     * - 한글 라벨: "식사 이미지만 분석" -> IMAGE_ONLY
     * - 한글 라벨: "식사 이미지 분석과 사용자 상태에 따라 제안" -> IMAGE_WITH_SUGGESTION
     * Null or blank defaults to IMAGE_WITH_SUGGESTION (backward compatibility).
     */
    public static AnalysisMode from(String value) {
        if (value == null || value.isBlank()) return IMAGE_WITH_SUGGESTION;
        String trimmed = value.trim();
        String up = trimmed.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        // English codes and enum names
        if ("IMG_ONLY".equals(up) || "IMAGE_ONLY".equals(up)) return IMAGE_ONLY;
        if ("IMG_SUGG".equals(up) || "IMAGE_WITH_SUGGESTION".equals(up)) return IMAGE_WITH_SUGGESTION;
        // Korean labels from requirements
        if ("식사 이미지만 분석".equals(trimmed)) return IMAGE_ONLY;
        if ("식사 이미지 분석과 사용자 상태에 따라 제안".equals(trimmed)) return IMAGE_WITH_SUGGESTION;
        throw new IllegalArgumentException("analysisMode must be IMG_ONLY or IMG_SUGG");
    }
}
