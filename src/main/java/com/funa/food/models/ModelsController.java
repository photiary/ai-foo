package com.funa.food.models;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/models", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Models", description = "Model info proxy API")
public class ModelsController {

    private final ModelsService modelsService;

    @Operation(summary = "List available models", description = "Proxies upstream OpenAI-compatible /v1/models and returns the JSON response.")
    @GetMapping
    public ResponseEntity<String> listModels() {
        String body = modelsService.fetchModels();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
