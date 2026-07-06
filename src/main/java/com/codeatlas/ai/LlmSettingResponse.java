package com.codeatlas.ai;

import java.time.Instant;

public record LlmSettingResponse(
        Long id,
        boolean enabled,
        String provider,
        String baseUrl,
        String apiKey,
        String modelName,
        Instant updatedAt
) {
    public LlmSettingResponse(LlmSetting setting) {
        this(
                setting.getId(),
                setting.isEnabled(),
                setting.getProvider(),
                setting.getBaseUrl(),
                setting.getApiKey(),
                setting.getModelName(),
                setting.getUpdatedAt()
        );
    }
}
