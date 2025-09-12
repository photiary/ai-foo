package com.funa.food.food;

import com.funa.food.food.dto.FoodAnalysisResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class FoodAnalysisControllerTest { 

    MockMvc mockMvc;
    FoodAnalysisService foodAnalysisService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        foodAnalysisService = Mockito.mock(FoodAnalysisService.class);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(new FoodAnalysisController(foodAnalysisService))
                .setControllerAdvice(new com.funa.food.common.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void analyze_returnsResponse() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "meal.jpg", "image/jpeg", new byte[]{1,2,3}
        );
        String status = "남성,64세,체중78kg,키167cm,공복혈당120";

        FoodAnalysisResponse resp = FoodAnalysisResponse.builder()
                .foods(List.of(
                        FoodAnalysisResponse.FoodItem.builder()
                                .name("비빔밥").protein(20).carbs(80).fat(10).calories(600)
                                .position(FoodAnalysisResponse.Position.builder().x(100).y(200).build())
                                .build()
                ))
                .suitability("중간")
                .suggestion("현미밥과 채소를 추가하세요")
                .build();

        Mockito.when(foodAnalysisService.analyze(Mockito.any(), Mockito.anyString(), Mockito.eq(AnalysisMode.IMAGE_WITH_SUGGESTION), Mockito.any()))
                .thenReturn(resp);

        mockMvc.perform(multipart("/v1/food/analysis")
                        .file(image)
                        .param("status", status)
                        .param("analysisMode", "식사 이미지 분석과 사용자 상태에 따라 제안")
                        .param("modelName", "local-model")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foods[0].name").value("비빔밥"))
                .andExpect(jsonPath("$.suitability").value("중간"));
    }
}
