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
    private static final Map<String, Rate> RATES = createRates();

    private static Map<String, Rate> createRates() {
        java.util.HashMap<String, Rate> m = new java.util.HashMap<>();
        // Prices per prompts/OpenAI-Pricing.md (per 1M tokens)
        m.put("gpt-5", new Rate(new BigDecimal("1.25"), new BigDecimal("10.00"), new BigDecimal("0.125")));
        m.put("gpt-5-mini", new Rate(new BigDecimal("0.25"), new BigDecimal("2.00"), new BigDecimal("0.025")));
        m.put("gpt-5-nano", new Rate(new BigDecimal("0.05"), new BigDecimal("0.40"), new BigDecimal("0.005")));
        m.put("gpt-5-chat-latest", new Rate(new BigDecimal("1.25"), new BigDecimal("10.00"), new BigDecimal("0.125")));
        m.put("gpt-4.1", new Rate(new BigDecimal("2.00"), new BigDecimal("8.00"), new BigDecimal("0.50")));
        m.put("gpt-4.1-mini", new Rate(new BigDecimal("0.40"), new BigDecimal("1.60"), new BigDecimal("0.10")));
        m.put("gpt-4.1-nano", new Rate(new BigDecimal("0.10"), new BigDecimal("0.40"), new BigDecimal("0.025")));
        m.put("gpt-4o", new Rate(new BigDecimal("2.50"), new BigDecimal("10.00"), new BigDecimal("1.25")));
        m.put("gpt-4o-2024-05-13", new Rate(new BigDecimal("5.00"), new BigDecimal("15.00"), BigDecimal.ZERO));
        m.put("gpt-4o-mini", new Rate(new BigDecimal("0.15"), new BigDecimal("0.60"), new BigDecimal("0.075")));
        m.put("gpt-realtime", new Rate(new BigDecimal("4.00"), new BigDecimal("16.00"), new BigDecimal("0.40")));
        m.put("gpt-4o-realtime-preview", new Rate(new BigDecimal("5.00"), new BigDecimal("20.00"), new BigDecimal("2.50")));
        m.put("gpt-4o-mini-realtime-preview", new Rate(new BigDecimal("0.60"), new BigDecimal("2.40"), new BigDecimal("0.30")));
        m.put("gpt-audio", new Rate(new BigDecimal("2.50"), new BigDecimal("10.00"), BigDecimal.ZERO));
        m.put("gpt-4o-audio-preview", new Rate(new BigDecimal("2.50"), new BigDecimal("10.00"), BigDecimal.ZERO));
        m.put("gpt-4o-mini-audio-preview", new Rate(new BigDecimal("0.15"), new BigDecimal("0.60"), BigDecimal.ZERO));
        m.put("o1", new Rate(new BigDecimal("15.00"), new BigDecimal("60.00"), new BigDecimal("7.50")));
        m.put("o1-pro", new Rate(new BigDecimal("150.00"), new BigDecimal("600.00"), BigDecimal.ZERO));
        m.put("o3-pro", new Rate(new BigDecimal("20.00"), new BigDecimal("80.00"), BigDecimal.ZERO));
        m.put("o3", new Rate(new BigDecimal("2.00"), new BigDecimal("8.00"), new BigDecimal("0.50")));
        m.put("o3-deep-research", new Rate(new BigDecimal("10.00"), new BigDecimal("40.00"), new BigDecimal("2.50")));
        m.put("o4-mini", new Rate(new BigDecimal("1.10"), new BigDecimal("4.40"), new BigDecimal("0.275")));
        m.put("o4-mini-deep-research", new Rate(new BigDecimal("2.00"), new BigDecimal("8.00"), new BigDecimal("0.50")));
        m.put("o3-mini", new Rate(new BigDecimal("1.10"), new BigDecimal("4.40"), new BigDecimal("0.55")));
        m.put("o1-mini", new Rate(new BigDecimal("1.10"), new BigDecimal("4.40"), new BigDecimal("0.55")));
        m.put("codex-mini-latest", new Rate(new BigDecimal("1.50"), new BigDecimal("6.00"), new BigDecimal("0.375")));
        m.put("gpt-4o-mini-search-preview", new Rate(new BigDecimal("0.15"), new BigDecimal("0.60"), BigDecimal.ZERO));
        m.put("gpt-4o-search-preview", new Rate(new BigDecimal("2.50"), new BigDecimal("10.00"), BigDecimal.ZERO));
        m.put("computer-use-preview", new Rate(new BigDecimal("3.00"), new BigDecimal("12.00"), BigDecimal.ZERO));
        // Image models
        m.put("gpt-image-1", new Rate(new BigDecimal("10.00"), new BigDecimal("40.00"), new BigDecimal("2.50")));
        // Aliases
        m.put("gpt-4o-2024-08-06", m.get("gpt-4o"));
        // Local model -> free
        m.put("local-model", new Rate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        return java.util.Collections.unmodifiableMap(m);
    }

    public static Cost estimate(String modelName, Integer promptTokens, Integer cachedTokens, Integer completionTokens, Integer totalTokens) {
        String key = modelName == null ? "local-model" : modelName;
        Rate rate = RATES.getOrDefault(key, RATES.get("local-model"));

        BigDecimal million = new BigDecimal("1000000");
        BigDecimal input = BigDecimal.ZERO;
        BigDecimal cachedInput = BigDecimal.ZERO;
        BigDecimal output = BigDecimal.ZERO;

        int cached = cachedTokens == null ? 0 : Math.max(0, cachedTokens);
        if (promptTokens != null) {
            int billablePrompt;
            if (rate.cachedInputRatePer1m.compareTo(BigDecimal.ZERO) <= 0) {
                // Model has no cached pricing: bill all prompt tokens as regular input, no cache cost
                billablePrompt = Math.max(0, promptTokens);
                cachedInput = BigDecimal.ZERO;
            } else {
                billablePrompt = Math.max(0, promptTokens - cached);
                cachedInput = rate.cachedInputRatePer1m.multiply(new BigDecimal(cached)).divide(million, 6, RoundingMode.HALF_UP);
            }
            input = rate.inputRatePer1m.multiply(new BigDecimal(billablePrompt)).divide(million, 6, RoundingMode.HALF_UP);
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

        BigDecimal total = input.add(output).add(cachedInput).setScale(6, RoundingMode.HALF_UP);
        return new Cost(
                input.setScale(6, RoundingMode.HALF_UP),
                cachedInput.setScale(6, RoundingMode.HALF_UP),
                output.setScale(6, RoundingMode.HALF_UP),
                total,
                "USD");
    }

    private record Rate(BigDecimal inputRatePer1m, BigDecimal outputRatePer1m, BigDecimal cachedInputRatePer1m) {}

    public record Cost(BigDecimal inputCost, BigDecimal cachedInputCost, BigDecimal outputCost, BigDecimal totalCost, String currency) {}
}
