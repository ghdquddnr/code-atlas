package com.codeatlas.ai;

public record LlmSettingRequest(
        boolean enabled,
        String provider,
        String baseUrl,
        String apiKey,
        String modelName
) {
    public LlmSetting toEntity() {
        return new LlmSetting(enabled, provider, baseUrl, apiKey, modelName);
    }
}
