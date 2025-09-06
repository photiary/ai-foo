package com.funa.food.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funa.food.chat.dto.ChatRequest;
import com.funa.food.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper;
    ChatService chatService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        chatService = Mockito.mock(ChatService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(chatService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void chat_returns_response() throws Exception {
        given(chatService.complete(anyString())).willReturn("hello");
        var req = new ChatRequest("hi");
        mockMvc.perform(post("/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("hello"));
    }
}
