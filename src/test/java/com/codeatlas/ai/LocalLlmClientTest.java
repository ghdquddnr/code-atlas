package com.codeatlas.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;

class LocalLlmClientTest {

    @Test
    void generatesTextWithConfiguredOllamaModel() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        LlmSettingRepository settingRepository = org.mockito.Mockito.mock(LlmSettingRepository.class);
        org.mockito.Mockito.when(settingRepository.findAll()).thenReturn(java.util.List.of());
        LocalLlmClient client = new LocalLlmClient(builder, settingRepository, new com.fasterxml.jackson.databind.ObjectMapper(), true, "http://ollama.test", "gemma4:e4b");

        server.expect(requestTo("http://ollama.test/api/generate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("gemma4:e4b"))
                .andExpect(jsonPath("$.stream").value(false))
                .andRespond(withSuccess("{\"response\":\"근거 기반 답변입니다.\"}", MediaType.APPLICATION_JSON));

        Optional<String> response = client.generate("근거만 사용해서 답변해줘.");

        assertThat(response).contains("근거 기반 답변입니다.");
        server.verify();
    }

    @Test
    void returnsEmptyWhenDisabled() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        LlmSettingRepository settingRepository = org.mockito.Mockito.mock(LlmSettingRepository.class);
        org.mockito.Mockito.when(settingRepository.findAll()).thenReturn(java.util.List.of(
                new LlmSetting(false, "OLLAMA", "http://ollama.test", "", "gemma4:e4b")
        ));
        LocalLlmClient client = new LocalLlmClient(builder, settingRepository, new com.fasterxml.jackson.databind.ObjectMapper(), false, "http://ollama.test", "gemma4:e4b");

        assertThat(client.generate("ignored")).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyWhenDefaultConfigurationIsDisabled() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        LlmSettingRepository settingRepository = org.mockito.Mockito.mock(LlmSettingRepository.class);
        org.mockito.Mockito.when(settingRepository.findAll()).thenReturn(java.util.List.of());
        LocalLlmClient client = new LocalLlmClient(builder, settingRepository, new com.fasterxml.jackson.databind.ObjectMapper(), false, "http://ollama.test", "gemma4:e4b");

        assertThat(client.generate("ignored")).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyWhenOllamaRequestFails() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        LlmSettingRepository settingRepository = org.mockito.Mockito.mock(LlmSettingRepository.class);
        org.mockito.Mockito.when(settingRepository.findAll()).thenReturn(java.util.List.of());
        LocalLlmClient client = new LocalLlmClient(builder, settingRepository, new com.fasterxml.jackson.databind.ObjectMapper(), true, "http://ollama.test", "gemma4:e4b");

        server.expect(requestTo("http://ollama.test/api/generate"))
                .andRespond(withServerError());

        assertThat(client.generate("fallback please")).isEmpty();
        server.verify();
    }

    @Test
    void generatesTextWithOpenAi() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        LocalLlmClient client = client(builder);

        server.expect(requestTo("https://openai.test/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("gpt-test"))
                .andExpect(jsonPath("$.messages[0].content").value("test prompt"))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"openai success\"}}]}", MediaType.APPLICATION_JSON));

        assertThat(client.generateWithSetting(
                new LlmSetting(true, "OPENAI", "https://openai.test", "test-key", "gpt-test"),
                "test prompt"
        )).isEqualTo("openai success");
        server.verify();
    }

    @Test
    void generatesTextWithAnthropic() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        LocalLlmClient client = client(builder);

        server.expect(requestTo("https://anthropic.test/v1/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-api-key", "test-key"))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andExpect(jsonPath("$.model").value("claude-test"))
                .andRespond(withSuccess("{\"content\":[{\"type\":\"text\",\"text\":\"anthropic success\"}]}", MediaType.APPLICATION_JSON));

        assertThat(client.generateWithSetting(
                new LlmSetting(true, "ANTHROPIC", "https://anthropic.test", "test-key", "claude-test"),
                "test prompt"
        )).isEqualTo("anthropic success");
        server.verify();
    }

    @Test
    void generatesTextWithGemini() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        LocalLlmClient client = client(builder);

        server.expect(requestTo("https://gemini.test/v1beta/models/gemini-test:generateContent?key=test-key"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.contents[0].parts[0].text").value("test prompt"))
                .andRespond(withSuccess("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"gemini success\"}]}}]}", MediaType.APPLICATION_JSON));

        assertThat(client.generateWithSetting(
                new LlmSetting(true, "GEMINI", "https://gemini.test", "test-key", "gemini-test"),
                "test prompt"
        )).isEqualTo("gemini success");
        server.verify();
    }

    private LocalLlmClient client(RestClient.Builder builder) {
        LlmSettingRepository repository = org.mockito.Mockito.mock(LlmSettingRepository.class);
        org.mockito.Mockito.when(repository.findAll()).thenReturn(java.util.List.of());
        return new LocalLlmClient(
                builder,
                repository,
                new com.fasterxml.jackson.databind.ObjectMapper(),
                true,
                "http://ollama.test",
                "gemma4:e4b"
        );
    }
}
