package com.funa.food.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModelsService {

    private static final Logger log = LoggerFactory.getLogger(ModelsService.class);

    private final RestClient restClient;

    private final String baseUrl;

    public ModelsService(RestClient.Builder builder,
                         @Value("${spring.ai.openai.base-url:http://127.0.0.1:1234}") String baseUrl) {
        this.baseUrl = baseUrl;
        // Apply builder customizations (timeouts, logging) via RestClientCustomizer auto-config
        this.restClient = builder.build();
    }

    public String fetchModels() {
        String target = baseUrl + "/v1/models";
        log.info("Fetching models from upstream: {}", target);
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(target)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);
            log.info("Upstream responded: status={}, bytes={}", response.getStatusCode().value(),
                    response.getBody() != null ? response.getBody().length() : 0);
            return response.getBody();
        } catch (RestClientResponseException ex) {
            // Propagate upstream HTTP status and body as reason
            log.warn("Upstream error: status={}, body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(ex.getStatusCode(), "Upstream /v1/models error");
        } catch (Exception ex) {
            log.error("Failed to fetch upstream models: {}", ex.getMessage());
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Failed to reach upstream /v1/models");
        }
    }
}
