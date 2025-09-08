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
     * Rates per 1M tokens in USD. Separate for input and output when applicable.
     */
    private static final Map<String, Rate> RATES = Map.of(
            // Example rates (not authoritative). Adjust to your pricing file as needed.
            // Per prompts/OpenAI-Pricing.md, prices are per 1M tokens
            "gpt-5", new Rate(new BigDecimal("1.25"), new BigDecimal("10.00")),
            "gpt-4.1-nano", new Rate(new BigDecimal("0.10"), new BigDecimal("0.40")),
            "gpt-4o", new Rate(new BigDecimal("2.50"), new BigDecimal("10.00")),
            "gpt-image-1", new Rate(new BigDecimal("10.00"), new BigDecimal("40.00")),
            // Local model or unknown -> free
            "local-model", new Rate(BigDecimal.ZERO, BigDecimal.ZERO),
            "unknown", new Rate(BigDecimal.ZERO, BigDecimal.ZERO)
    );

    public static Cost estimate(String modelName, Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        String key = modelName == null ? "unknown" : modelName;
        Rate rate = RATES.getOrDefault(key, RATES.get("unknown"));

        BigDecimal million = new BigDecimal("1000000");
        BigDecimal input = BigDecimal.ZERO;
        BigDecimal output = BigDecimal.ZERO;

        if (promptTokens != null) {
            input = rate.inputRatePer1m.multiply(new BigDecimal(promptTokens)).divide(million, 6, RoundingMode.HALF_UP);
        }
        if (completionTokens != null) {
            output = rate.outputRatePer1m.multiply(new BigDecimal(completionTokens)).divide(million, 6, RoundingMode.HALF_UP);
        }
        // If only total tokens provided, split half/half heuristically (best-effort)
        if (promptTokens == null && completionTokens == null && totalTokens != null) {
            BigDecimal half = new BigDecimal(totalTokens).divide(new BigDecimal("2"), 6, RoundingMode.HALF_UP);
            input = rate.inputRatePer1m.multiply(half).divide(million, 6, RoundingMode.HALF_UP);
            output = rate.outputRatePer1m.multiply(half).divide(million, 6, RoundingMode.HALF_UP);
        }

        BigDecimal total = input.add(output).setScale(6, RoundingMode.HALF_UP);
        return new Cost(input.setScale(6, RoundingMode.HALF_UP), output.setScale(6, RoundingMode.HALF_UP), total, "USD");
    }

    private record Rate(BigDecimal inputRatePer1m, BigDecimal outputRatePer1m) {}

    public record Cost(BigDecimal inputCost, BigDecimal outputCost, BigDecimal totalCost, String currency) {}
}
