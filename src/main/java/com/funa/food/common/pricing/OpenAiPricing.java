package com.funa.food.common.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Simple pricing helper using prompts/OpenAI-Pricing.md guidance.
 * This is intentionally minimal and can be extended per real pricing tables.
 */
public final class OpenAiPricing {

    private OpenAiPricing() {}

    /**
     * Rates per 1K tokens in USD. Separate for input and output when applicable.
     */
    private static final Map<String, Rate> RATES = Map.of(
            // Example rates (not authoritative). Adjust to your pricing file as needed.
            "gpt-4.1-nano", new Rate(new BigDecimal("0.0004"), new BigDecimal("0.0012")),
            // Local model or unknown -> free
            "local-model", new Rate(BigDecimal.ZERO, BigDecimal.ZERO),
            "unknown", new Rate(BigDecimal.ZERO, BigDecimal.ZERO)
    );

    public static Cost estimate(String modelName, Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        String key = modelName == null ? "unknown" : modelName;
        Rate rate = RATES.getOrDefault(key, RATES.get("unknown"));

        BigDecimal thousand = new BigDecimal("1000");
        BigDecimal input = BigDecimal.ZERO;
        BigDecimal output = BigDecimal.ZERO;

        if (promptTokens != null) {
            input = rate.inputRatePer1k.multiply(new BigDecimal(promptTokens)).divide(thousand, 6, RoundingMode.HALF_UP);
        }
        if (completionTokens != null) {
            output = rate.outputRatePer1k.multiply(new BigDecimal(completionTokens)).divide(thousand, 6, RoundingMode.HALF_UP);
        }
        // If only total tokens provided, split half/half heuristically (best-effort)
        if (promptTokens == null && completionTokens == null && totalTokens != null) {
            BigDecimal half = new BigDecimal(totalTokens).divide(new BigDecimal("2"), 6, RoundingMode.HALF_UP);
            input = rate.inputRatePer1k.multiply(half).divide(thousand, 6, RoundingMode.HALF_UP);
            output = rate.outputRatePer1k.multiply(half).divide(thousand, 6, RoundingMode.HALF_UP);
        }

        BigDecimal total = input.add(output).setScale(6, RoundingMode.HALF_UP);
        return new Cost(input.setScale(6, RoundingMode.HALF_UP), output.setScale(6, RoundingMode.HALF_UP), total, "USD");
    }

    private record Rate(BigDecimal inputRatePer1k, BigDecimal outputRatePer1k) {}

    public record Cost(BigDecimal inputCost, BigDecimal outputCost, BigDecimal totalCost, String currency) {}
}
