package com.codeatlas.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "llm_settings")
public class LlmSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, length = 50)
    private String provider; // OLLAMA, OPENAI, ANTHROPIC, GEMINI

    @Column(length = 1000)
    private String baseUrl;

    @Column(length = 1000)
    private String apiKey;

    @Column(nullable = false, length = 100)
    private String modelName;

    @Column(nullable = false)
    private Instant updatedAt;

    protected LlmSetting() {
    }

    public LlmSetting(boolean enabled, String provider, String baseUrl, String apiKey, String modelName) {
        this.enabled = enabled;
        this.provider = provider;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.updatedAt = Instant.now();
    }

    public static LlmSetting createDefault(String defaultBaseUrl, String defaultModel) {
        return new LlmSetting(true, "OLLAMA", defaultBaseUrl, "", defaultModel);
    }

    public void update(boolean enabled, String provider, String baseUrl, String apiKey, String modelName) {
        this.enabled = enabled;
        this.provider = provider;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getProvider() {
        return provider;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
