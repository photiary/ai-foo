package com.funa.food.food;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AnalysisModeConverter implements AttributeConverter<AnalysisMode, String> {

    @Override
    public String convertToDatabaseColumn(AnalysisMode attribute) {
        if (attribute == null) return null;
        return switch (attribute) {
            case IMAGE_ONLY -> "IMG_ONLY";
            case IMAGE_WITH_SUGGESTION -> "IMG_SUGG";
        };
    }

    @Override
    public AnalysisMode convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return AnalysisMode.IMAGE_WITH_SUGGESTION;
        String val = dbData.trim();
        // Accept both codes and legacy enum names to be resilient during migration
        return switch (val) {
            case "IMG_ONLY", "IMAGE_ONLY" -> AnalysisMode.IMAGE_ONLY;
            case "IMG_SUGG", "IMAGE_WITH_SUGGESTION" -> AnalysisMode.IMAGE_WITH_SUGGESTION;
            default -> throw new IllegalArgumentException("Unknown analysis_mode: " + dbData);
        };
    }
}
