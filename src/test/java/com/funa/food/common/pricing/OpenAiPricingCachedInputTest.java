package com.funa.food.common.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAiPricingCachedInputTest {

    @Test
    void testEstimate_gpt5_withCachedTokens() {
        String model = "gpt-5";
        int prompt = 1000;
        int cached = 400;
        int completion = 0;
        OpenAiPricing.Cost cost = OpenAiPricing.estimate(model, prompt, cached, completion, null);
        // input: (1000-400)=600 @ $1.25/M => 1.25*600/1_000_000 = 0.000750 -> 0.000001? wait: 1.25*600/1_000_000 = 0.000750
        // cache: 400 @ $0.125/M => 0.125*400/1_000_000 = 0.000050
        assertEquals(new BigDecimal("0.000750"), cost.inputCost());
        assertEquals(new BigDecimal("0.000050"), cost.cachedInputCost());
        assertEquals(new BigDecimal("0.000000"), cost.outputCost());
        assertEquals(new BigDecimal("0.000800"), cost.totalCost());
        assertEquals("USD", cost.currency());
    }

    @Test
    void testEstimate_gpt4o_2024_05_13_noCachedPricing() {
        String model = "gpt-4o-2024-05-13";
        int prompt = 1000;
        int cached = 400; // should be ignored since model has '-' for cached pricing
        int completion = 0;
        OpenAiPricing.Cost cost = OpenAiPricing.estimate(model, prompt, cached, completion, null);
        // input: all 1000 @ $5.00/M => 0.005000
        // cache: 0
        assertEquals(new BigDecimal("0.005000"), cost.inputCost());
        assertEquals(new BigDecimal("0.000000"), cost.cachedInputCost());
        assertEquals(new BigDecimal("0.000000"), cost.outputCost());
        assertEquals(new BigDecimal("0.005000"), cost.totalCost());
    }
}
