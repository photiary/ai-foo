package com.funa.food.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Local model ChatClient configuration.
 * - Bean name "localChatClient" is used by FoodAnalysisService when modelName == 'local-model'.
 * - Points to a local OpenAI-compatible server (e.g., LM Studio) at http://localhost:1234
 * - Uses API key "dummy" by default.
 */
@Configuration
public class ChatClientConfig {

    private static final String localAiBaseUrl = System.getProperty("local.ai.openai.base-url", "http://localhost:1234");

    @Bean
    public ChatClient openChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.create(openAiChatModel);
    }

    @Bean
    public ChatClient localChatClient(ResponseErrorHandler responseErrorHandler,
                                      RestClient.Builder restClientBuilder,
                                      WebClient.Builder webClientBuilder) {
        OpenAiApi openAiApi = new OpenAiApi.Builder()
                .apiKey("dummy")
                .baseUrl(localAiBaseUrl)
                .responseErrorHandler(responseErrorHandler)
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilder)
                .build();
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder().openAiApi(openAiApi).build();
        return ChatClient.create(openAiChatModel);
    }
}
