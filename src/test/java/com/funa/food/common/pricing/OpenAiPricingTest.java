package com.funa.food.common.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAiPricingTest {

    @Test
    void testEstimate_gpt41nano_usageExample() {
        // Given usage payload:
        // {
        //   "usage": {
        //     "prompt_tokens": 203,
        //     "completion_tokens": 559,
        //     "total_tokens": 762
        //   }
        // }
        String model = "gpt-4.1-nano";
        Integer promptTokens = 203;
        Integer completionTokens = 559;
        Integer totalTokens = 762; // Should be ignored when prompt/completion provided

        OpenAiPricing.Cost cost = OpenAiPricing.estimate(model, promptTokens, 0, completionTokens, totalTokens);

        // Expected per 1M token pricing:
        // input $0.10/M, output $0.40/M, cached $0.04/M
        // cachedTokens=0, so cachedInputCost=0
        // input: 0.10 * 203 / 1_000_000 = 0.0000203 -> scale(6) = 0.000020
        // output: 0.40 * 559 / 1_000_000 = 0.0002236 -> HALF_UP to 6 = 0.000224
        // total: 0.000244
        assertEquals(new BigDecimal("0.000020"), cost.inputCost());
        assertEquals(new BigDecimal("0.000000"), cost.cachedInputCost());
        assertEquals(new BigDecimal("0.000224"), cost.outputCost());
        assertEquals(new BigDecimal("0.000244"), cost.totalCost());
        assertEquals("USD", cost.currency());
    }
}
