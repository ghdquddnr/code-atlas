package com.codeatlas.ai;

import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class LocalLlmClient {

    private static final Logger log = LoggerFactory.getLogger(LocalLlmClient.class);

    private final RestClient restClient;
    private final boolean enabled;
    private final String model;

    public LocalLlmClient(
            RestClient.Builder restClientBuilder,
            @Value("${code-atlas.llm.enabled:true}") boolean enabled,
            @Value("${code-atlas.llm.base-url:http://localhost:11434}") String baseUrl,
            @Value("${code-atlas.llm.model:gemma4:e4b}") String model
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.enabled = enabled;
        this.model = model;
    }

    public Optional<String> generate(String prompt) {
        if (!enabled || prompt == null || prompt.isBlank()) {
            return Optional.empty();
        }

        try {
            OllamaGenerateResponse response = restClient.post()
                    .uri("/api/generate")
                    .body(new OllamaGenerateRequest(
                            model,
                            prompt,
                            false,
                            Map.of("temperature", 0.2)
                    ))
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (response == null || response.response() == null || response.response().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(response.response().trim());
        } catch (RestClientException exception) {
            log.warn("Local LLM generation failed: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    private record OllamaGenerateRequest(
            String model,
            String prompt,
            boolean stream,
            Map<String, Object> options
    ) {
    }

    private record OllamaGenerateResponse(String response) {
    }
}
