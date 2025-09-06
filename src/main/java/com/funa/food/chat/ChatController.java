package com.funa.food.chat;

import com.funa.food.chat.dto.ChatRequest;
import com.funa.food.chat.dto.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/chat", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat completion API")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Chat completion", description = "Calls OpenAI /v1/chat/completions and returns the full response once completed.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String output = chatService.complete(request.getContent());
        return ResponseEntity.ok(ChatResponse.builder().response(output).build());
    }
}
