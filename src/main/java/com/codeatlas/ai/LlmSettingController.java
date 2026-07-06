package com.codeatlas.ai;

import com.codeatlas.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/llm/settings")
public class LlmSettingController {

    private final LlmSettingService service;
    private final LocalLlmClient client;

    public LlmSettingController(LlmSettingService service, LocalLlmClient client) {
        this.service = service;
        this.client = client;
    }

    @GetMapping
    public ApiResponse<LlmSettingResponse> getSettings() {
        LlmSetting setting = service.getSettings();
        return ApiResponse.ok(new LlmSettingResponse(setting));
    }

    @PostMapping
    public ApiResponse<LlmSettingResponse> updateSettings(@RequestBody LlmSettingRequest request) {
        LlmSetting setting = service.updateSettings(
                request.enabled(),
                request.provider(),
                request.baseUrl(),
                request.apiKey(),
                request.modelName()
        );
        return ApiResponse.ok(new LlmSettingResponse(setting));
    }

    @PostMapping("/test")
    public ApiResponse<LlmTestResponse> testConnection(@RequestBody LlmSettingRequest request) {
        String testPrompt = "connection test. reply with 'success' and nothing else.";
        try {
            String result = client.generateWithSetting(request.toEntity(), testPrompt);
            return ApiResponse.ok(new LlmTestResponse(true, result));
        } catch (Exception exception) {
            return ApiResponse.ok(new LlmTestResponse(false, exception.getMessage()));
        }
    }
}
