package com.codeatlas.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LocalLlmClient {

    private static final Logger log = LoggerFactory.getLogger(LocalLlmClient.class);

    private final RestClient restClient;
    private final LlmSettingRepository settingRepository;
    private final ObjectMapper objectMapper;
    private final boolean defaultEnabled;
    private final String defaultBaseUrl;
    private final String defaultModel;

    public LocalLlmClient(
            RestClient.Builder restClientBuilder,
            LlmSettingRepository settingRepository,
            ObjectMapper objectMapper,
            @Value("${code-atlas.llm.enabled:true}") boolean defaultEnabled,
            @Value("${code-atlas.llm.base-url:http://localhost:11434}") String defaultBaseUrl,
            @Value("${code-atlas.llm.model:gemma4:e4b}") String defaultModel
    ) {
        this.restClient = restClientBuilder.build();
        this.settingRepository = settingRepository;
        this.objectMapper = objectMapper;
        this.defaultEnabled = defaultEnabled;
        this.defaultBaseUrl = defaultBaseUrl;
        this.defaultModel = defaultModel;
    }

    private LlmSetting getActiveSetting() {
        return settingRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> LlmSetting.createDefault(defaultBaseUrl, defaultModel));
    }

    public Optional<String> generate(String prompt) {
        LlmSetting activeSetting = getActiveSetting();
        if (!activeSetting.isEnabled() || prompt == null || prompt.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(generateWithSetting(activeSetting, prompt));
        } catch (Exception exception) {
            log.warn("LLM generation failed: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    public String generateWithSetting(LlmSetting setting, String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "";
        }

        String provider = setting.getProvider().toUpperCase(java.util.Locale.ROOT);
        String baseUrl = setting.getBaseUrl();
        String apiKey = setting.getApiKey();
        String modelName = setting.getModelName();

        return switch (provider) {
            case "OLLAMA" -> callOllama(baseUrl, modelName, prompt);
            case "OPENAI" -> callOpenAi(baseUrl, apiKey, modelName, prompt);
            case "ANTHROPIC" -> callAnthropic(baseUrl, apiKey, modelName, prompt);
            case "GEMINI" -> callGemini(baseUrl, apiKey, modelName, prompt);
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }

    private byte[] executePost(String url, java.util.function.Consumer<RestClient.RequestBodySpec> specCustomizer, Object body) {
        try {
            RestClient.RequestBodySpec spec = restClient.post().uri(url);
            if (specCustomizer != null) {
                specCustomizer.accept(spec);
            }
            return spec.body(body)
                    .exchange((request, response) -> {
                        byte[] bytes;
                        try (java.io.InputStream is = response.getBody()) {
                            bytes = is.readAllBytes();
                        }
                        if (response.getStatusCode().isError()) {
                            throw new java.io.IOException("HTTP error " + response.getStatusCode() + ": " + new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
                        }
                        return bytes;
                    });
        } catch (Exception e) {
            throw new IllegalStateException("API call failed: " + e.getMessage(), e);
        }
    }

    private String callOllama(String baseUrl, String modelName, String prompt) {
        String url = (baseUrl == null || baseUrl.isBlank()) ? "http://localhost:11434" : baseUrl;
        url = url.endsWith("/") ? url + "api/generate" : url + "/api/generate";

        Map<String, Object> body = Map.of(
                "model", modelName,
                "prompt", prompt,
                "stream", false,
                "options", Map.of("temperature", 0.2)
        );

        byte[] responseBytes = executePost(url, null, body);
        String responseText = responseBytes == null ? "" : new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);

        try {
            OllamaResponse response = objectMapper.readValue(responseText, OllamaResponse.class);
            if (response == null || response.response() == null) {
                throw new IllegalStateException("Ollama returned empty response");
            }
            return response.response().trim();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Ollama JSON: " + e.getMessage() + ". Response was: " + responseText, e);
        }
    }

    private String callOpenAi(String baseUrl, String apiKey, String modelName, String prompt) {
        String url = (baseUrl == null || baseUrl.isBlank()) ? "https://api.openai.com" : baseUrl;
        url = url.endsWith("/") ? url + "v1/chat/completions" : url + "/v1/chat/completions";

        Map<String, Object> body = Map.of(
                "model", modelName,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.2
        );

        byte[] responseBytes = executePost(url, spec -> spec.header("Authorization", "Bearer " + apiKey), body);
        String responseText = responseBytes == null ? "" : new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);

        try {
            OpenAiResponse response = objectMapper.readValue(responseText, OpenAiResponse.class);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new IllegalStateException("OpenAI returned empty response");
            }
            return response.choices().get(0).message().content().trim();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI JSON: " + e.getMessage() + ". Response was: " + responseText, e);
        }
    }

    private String callAnthropic(String baseUrl, String apiKey, String modelName, String prompt) {
        String url = (baseUrl == null || baseUrl.isBlank()) ? "https://api.anthropic.com" : baseUrl;
        url = url.endsWith("/") ? url + "v1/messages" : url + "/v1/messages";

        Map<String, Object> body = Map.of(
                "model", modelName,
                "max_tokens", 4000,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.2
        );

        byte[] responseBytes = executePost(url, spec -> spec
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01"), body);
        String responseText = responseBytes == null ? "" : new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);

        try {
            AnthropicResponse response = objectMapper.readValue(responseText, AnthropicResponse.class);
            if (response == null || response.content() == null || response.content().isEmpty()) {
                throw new IllegalStateException("Anthropic returned empty response");
            }
            return response.content().get(0).text().trim();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Anthropic JSON: " + e.getMessage() + ". Response was: " + responseText, e);
        }
    }

    private String callGemini(String baseUrl, String apiKey, String modelName, String prompt) {
        String url = (baseUrl == null || baseUrl.isBlank()) ? "https://generativelanguage.googleapis.com" : baseUrl;
        url = url.endsWith("/")
                ? url + "v1beta/models/" + modelName + ":generateContent?key=" + apiKey
                : url + "/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of("temperature", 0.2)
        );

        byte[] responseBytes = executePost(url, null, body);
        String responseText = responseBytes == null ? "" : new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);

        try {
            GeminiResponse response = objectMapper.readValue(responseText, GeminiResponse.class);
            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                throw new IllegalStateException("Gemini returned empty response");
            }
            return response.candidates().get(0).content().parts().get(0).text().trim();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Gemini JSON: " + e.getMessage() + ". Response was: " + responseText, e);
        }
    }

    private record OllamaResponse(String response) {}

    private record OpenAiResponse(List<Choice> choices) {
        private record Choice(Message message) {}
        private record Message(String content) {}
    }

    private record AnthropicResponse(List<Content> content) {
        private record Content(String type, String text) {}
    }

    private record GeminiResponse(List<Candidate> candidates) {
        private record Candidate(Content content) {}
        private record Content(List<Part> parts) {}
        private record Part(String text) {}
    }
}
