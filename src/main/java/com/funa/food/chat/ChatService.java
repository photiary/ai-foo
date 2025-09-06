package com.funa.food.chat;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;

    public String complete(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            throw new IllegalArgumentException("request must not be blank");
        }
        log.info("Handling chat request: {}", userInput);
        String content = chatClient
                .prompt()
                .user(userInput)
                .call()
                .content();
        log.info("Chat response ready ({} chars)", content != null ? content.length() : 0);
        return content;
    }
}
