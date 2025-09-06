package com.funa.food.models;

import com.funa.food.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ModelsControllerTest {

    MockMvc mockMvc;
    ModelsService modelsService;

    @BeforeEach
    void setUp() {
        modelsService = Mockito.mock(ModelsService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ModelsController(modelsService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_models_returns_upstream_json() throws Exception {
        String upstream = "{\"data\":[{\"id\":\"gpt-test\"}]}";
        given(modelsService.fetchModels()).willReturn(upstream);
        mockMvc.perform(get("/v1/models").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(upstream));
    }
}
