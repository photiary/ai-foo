package com.funa.food.food;

import com.funa.food.food.dto.FoodAnalysisResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FoodAnalysisControllerMissingModelTest {

    private MockMvc mockMvc;
    private FoodAnalysisService foodAnalysisService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        foodAnalysisService = Mockito.mock(FoodAnalysisService.class);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(new FoodAnalysisController(foodAnalysisService))
                .setControllerAdvice(new com.funa.food.common.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void analyze_missingModelName_returnsBadRequest() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "food.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fakeimage".getBytes(StandardCharsets.UTF_8)
        );

        // no stubbing needed; controller should fail before service invocation
        mockMvc.perform(multipart("/v1/food/analysis")
                        .file(image)
                        .param("status", "피곤해요")
                        .param("analysisMode", "식사 이미지 분석과 사용자 상태에 따라 제안")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(foodAnalysisService);
    }
}
