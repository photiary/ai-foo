package com.funa.food.food;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funa.food.common.pricing.OpenAiPricing;
import com.funa.food.food.dto.FoodAnalysisResponse;
import com.funa.food.usage.UsageToken;
import com.funa.food.usage.UsageTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FoodAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(FoodAnalysisService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AnalysisFoodRepository analysisFoodRepository;
    private final UsageTokenRepository usageTokenRepository;

    @Value("${app.upload.dir:data/uploads}")
    private String uploadDir;

    @Transactional
    public FoodAnalysisResponse analyze(MultipartFile image, String status) {
        validateInputs(image, status);

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

            ImageMeta imageMeta = persistImageAndExtractMeta(image);

            OpenAiChatOptions options = buildChatOptionsWithSchema();

            ModelCallResult callResult = callModel(image, persona, userText, options);
            long durationMs = callResult.durationMs();
            FoodAnalysisResponse parsed = parseResponse(callResult.content());

            String modelName = resolveModelName();
            UsageToken usage = persistUsage(durationMs, modelName);

            persistAnalysisFood(parsed, status, imageMeta, usage);

            enrichResponseWithUsageAndBilling(parsed, usage, modelName);

            return parsed;
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid image or response parsing failed: " + e.getMessage(), e);
        }
    }

    private void validateInputs(MultipartFile image, String status) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("image must not be empty");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }
    }

    private record ImageMeta(String originalName, String uuidName, Path path, long size, String dimensions, String contentType) {}

    private ImageMeta persistImageAndExtractMeta(MultipartFile image) throws IOException {
        String originalName = image.getOriginalFilename();
        String uuidName = UUID.randomUUID() + extractExtension(originalName);
        ensureUploadDir();
        Path imagePath = Path.of(uploadDir, uuidName);
        Files.copy(image.getInputStream(), imagePath);

        String imageSize = null;
        try {
            BufferedImage bi = ImageIO.read(image.getInputStream());
            if (bi != null) {
                imageSize = bi.getWidth() + "x" + bi.getHeight();
            }
        } catch (Exception ignore) {
            // keep null if failed
        }
        return new ImageMeta(originalName, uuidName, imagePath, image.getSize(), imageSize, image.getContentType());
    }

    private OpenAiChatOptions buildChatOptionsWithSchema() throws IOException {
        String schemaJson = loadClasspath("schema/food-analysis-schema.json");
        ResponseFormat.JsonSchema jsonSchema = ResponseFormat.JsonSchema.builder()
                .name("FoodAnalysisResponse")
                .schema(schemaJson)
                .build();
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(jsonSchema)
                .build();
        return OpenAiChatOptions.builder()
                .responseFormat(responseFormat)
                .build();
    }

    private record ModelCallResult(String content, long durationMs) {}

    private ModelCallResult callModel(MultipartFile image, String persona, String userText, OpenAiChatOptions options) {
        Instant start = Instant.now();
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
        long durationMs = Duration.between(start, Instant.now()).toMillis();
        return new ModelCallResult(content, durationMs);
    }

    private FoodAnalysisResponse parseResponse(String content) throws IOException {
        return objectMapper.readValue(content, FoodAnalysisResponse.class);
    }

    private String resolveModelName() {
        return System.getProperty("OPENAI_MODEL", System.getenv().getOrDefault("OPENAI_MODEL", "unknown"));
    }

    private UsageToken persistUsage(long durationMs, String modelName) {
        UsageToken usage = UsageToken.builder()
                .modelName(modelName)
                .promptTokens(null)
                .completionTokens(null)
                .totalTokens(null)
                .requestDuration(durationMs)
                .build();
        return usageTokenRepository.save(usage);
    }

    private void persistAnalysisFood(FoodAnalysisResponse parsed, String status, ImageMeta imageMeta, UsageToken usage) {
        AnalysisFood af = AnalysisFood.builder()
                .userStatus(status)
                .foods(toJsonSilently(parsed.getFoods()))
                .suitability(parsed.getSuitability())
                .suggestion(parsed.getSuggestion())
                .imageUserFileName(imageMeta.originalName())
                .imageFileName(imageMeta.uuidName())
                .imageFileSize(imageMeta.size())
                .imageSize(imageMeta.dimensions())
                .usageToken(usage)
                .build();
        analysisFoodRepository.save(af);
    }

    private void enrichResponseWithUsageAndBilling(FoodAnalysisResponse parsed, UsageToken usage, String modelName) {
        FoodAnalysisResponse.UsageInfo usageInfo = FoodAnalysisResponse.UsageInfo.builder()
                .promptTokens(usage.getPromptTokens())
                .completionTokens(usage.getCompletionTokens())
                .totalTokens(usage.getTotalTokens())
                .modelName(usage.getModelName())
                .requestDurationMs(usage.getRequestDuration())
                .build();
        OpenAiPricing.Cost cost = OpenAiPricing.estimate(
                modelName,
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens()
        );
        FoodAnalysisResponse.BillingInfo billing = FoodAnalysisResponse.BillingInfo.builder()
                .inputCost(cost.inputCost())
                .outputCost(cost.outputCost())
                .totalCost(cost.totalCost())
                .currency(cost.currency())
                .build();
        parsed.setUsage(usageInfo);
        parsed.setBilling(billing);
    }

    private String toJsonSilently(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private void ensureUploadDir() throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            Files.createDirectories(dir.toPath());
        }
    }

    private String extractExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx) : "";
    }

    private String loadClasspath(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (var is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
