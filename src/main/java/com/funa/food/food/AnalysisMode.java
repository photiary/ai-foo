package com.funa.food.food;

import java.util.Locale;

public enum AnalysisMode {
    IMAGE_ONLY,
    IMAGE_WITH_SUGGESTION;

    /**
     * Parse analysis mode from request parameter.
     * Only two codes are allowed (case-insensitive):
     * - IMG_ONLY
     * - IMG_SUGG
     * Null or blank defaults to IMAGE_WITH_SUGGESTION (backward compatibility).
     * Any other value will cause IllegalArgumentException.
     */
    public static AnalysisMode from(String value) {
        if (value == null || value.isBlank()) return IMAGE_WITH_SUGGESTION;
        String up = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if ("IMG_ONLY".equals(up)) return IMAGE_ONLY;
        if ("IMG_SUGG".equals(up)) return IMAGE_WITH_SUGGESTION;
        throw new IllegalArgumentException("analysisMode must be IMG_ONLY or IMG_SUGG");
    }
}
