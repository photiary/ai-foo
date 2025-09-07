package com.funa.food.food;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funa.food.food.dto.FoodAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class FoodAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(FoodAnalysisService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FoodAnalysisResponse analyze(MultipartFile image, String status) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("image must not be empty");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }

        String persona = """
                너는 사용자의 생활과 식습관을 살펴보고, 작은 변화로도 건강한 하루를 이어갈 수 있도록 함께 응원하고 도와주는 AI 생활 코치이다.
                응답은 반드시 JSON만 출력한다.
                """;

        String userTextTemplate = """
                사용자 상태: %s
                식사 이미지 데이터 URI를 참고하여, 다음 정보를 JSON으로만 응답하라.
                - 음식 리스트
                - 음식별 탄수화물(carbs), 단백질(protein), 지방(fat) 단위: g
                - 음식별 칼로리(calories) 단위: kcal
                - 이미지에서의 위치 중심 좌표(position: {x, y})
                또한 사용자의 상태를 고려하여 식사의 적합성(suitability)을 평가하고 더 좋은 식사 제안(suggestion)을 제공하라.
                """;

        try {
            String userText = String.format(userTextTemplate, status);

            // Load JSON schema from classpath and apply per-request options
            String schemaJson = loadClasspath("schema/food-analysis-schema.json");
            ResponseFormat.JsonSchema jsonSchema = ResponseFormat.JsonSchema.builder()
                    .name("FoodAnalysisResponse")
                    .schema(schemaJson)
                    .build();
            ResponseFormat responseFormat = ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_SCHEMA)
                    .jsonSchema(jsonSchema)
                    .build();
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .responseFormat(responseFormat)
                    .build();

            String content = chatClient
                    .prompt()
                    .system(persona)
                    .user(u -> {
                        try {
                            u.text(userText).media(
                                    MediaType.parseMediaType(image.getContentType()),
                                    new InputStreamResource(image.getInputStream())
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .options(options)
                    .call()
                    .content();

            return objectMapper.readValue(content, FoodAnalysisResponse.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid image or response parsing failed: " + e.getMessage(), e);
        }
    }

    private String loadClasspath(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (var is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
