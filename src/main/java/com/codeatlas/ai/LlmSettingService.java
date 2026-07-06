package com.codeatlas.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LlmSettingService {

    private final LlmSettingRepository repository;
    private final String defaultBaseUrl;
    private final String defaultModel;

    public LlmSettingService(
            LlmSettingRepository repository,
            @Value("${code-atlas.llm.base-url:http://localhost:11434}") String defaultBaseUrl,
            @Value("${code-atlas.llm.model:gemma4:e4b}") String defaultModel
    ) {
        this.repository = repository;
        this.defaultBaseUrl = defaultBaseUrl;
        this.defaultModel = defaultModel;
    }

    @Transactional
    public LlmSetting getSettings() {
        return repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> repository.save(LlmSetting.createDefault(defaultBaseUrl, defaultModel)));
    }

    @Transactional
    public LlmSetting updateSettings(boolean enabled, String provider, String baseUrl, String apiKey, String modelName) {
        LlmSetting setting = getSettings();
        setting.update(enabled, provider, baseUrl, apiKey, modelName);
        return repository.save(setting);
    }
}
