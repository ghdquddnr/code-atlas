package com.codeatlas.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiContractIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    ApiContractIntegrationTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void servesStaticDashboardUi() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CodeAtlas")))
                .andExpect(content().string(containsString("root")))
                .andExpect(content().string(containsString("assets/index-")));
    }

    @Test
    void exposesProjectAnalysisAndResultApisOverHttp() throws Exception {
        Long projectId = createSampleProject();

        mockMvc.perform(post("/api/projects/{projectId}/analyze", projectId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.message", containsString("Spring APIs")))
                .andExpect(jsonPath("$.data.message", containsString("API flows")));

        mockMvc.perform(get("/api/projects/{projectId}/dashboard", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.sourceFileCount").value(8))
                .andExpect(jsonPath("$.data.springApiCount").value(5))
                .andExpect(jsonPath("$.data.myBatisStatementCount").value(5))
                .andExpect(jsonPath("$.data.apiFlowCount").value(7))
                .andExpect(jsonPath("$.data.tables", hasSize(3)));

        mockMvc.perform(get("/api/projects/{projectId}/source-files", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.className == 'OrderController' && @.classCategory == 'CONTROLLER')]").exists())
                .andExpect(jsonPath("$.data[?(@.className == 'CreateOrderRequest' && @.classCategory == 'DTO')]").exists())
                .andExpect(jsonPath("$.data[?(@.className == 'CreateOrderRequest')].classFields[0].name").value(hasItem("customerId")))
                .andExpect(jsonPath("$.data[?(@.className == 'OrderMapper' && @.classCategory == 'MAPPER')]").exists());

        mockMvc.perform(get("/api/projects/{projectId}/apis", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data[?(@.httpMethod == 'POST' && @.path == '/api/orders')]").exists())
                .andExpect(jsonPath("$.data[?(@.methodName == 'createOrder')].requestDtoName").value(hasItem("CreateOrderRequest")))
                .andExpect(jsonPath("$.data[?(@.methodName == 'createOrder')].responseDtoName").value(hasItem("OrderResponse")));

        Long createOrderApiId = findCreateOrderApiId(projectId);
        mockMvc.perform(get("/api/projects/{projectId}/apis/{apiId}", projectId, createOrderApiId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.api.methodName").value("createOrder"))
                .andExpect(jsonPath("$.data.flows", hasSize(2)));

        mockMvc.perform(get("/api/projects/{projectId}/tables/{tableName}", projectId, "tb_order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tableName").value("TB_ORDER"))
                .andExpect(jsonPath("$.data.statements", hasSize(5)))
                .andExpect(jsonPath("$.data.flows", hasSize(7)));

        mockMvc.perform(get("/api/projects/{projectId}/graph", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes", hasSize(29)))
                .andExpect(jsonPath("$.data.edges", hasSize(41)));

        mockMvc.perform(post("/api/projects/{projectId}/analyze", projectId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(get("/api/projects/{projectId}/analysis/comparison/latest", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.baseSnapshotId").exists())
                .andExpect(jsonPath("$.data.targetSnapshotId").exists())
                .andExpect(jsonPath("$.data.apis.addedCount").value(0))
                .andExpect(jsonPath("$.data.sqlStatements.removedCount").value(0))
                .andExpect(jsonPath("$.data.dtos.added", hasSize(0)))
                .andExpect(jsonPath("$.data.flows.removed", hasSize(0)))
                .andExpect(jsonPath("$.data.releaseRisk.readinessScore").value(100))
                .andExpect(jsonPath("$.data.releaseRisk.riskLevel").value("NONE"));

        MvcResult snapshotsResult = mockMvc.perform(get("/api/projects/{projectId}/analysis/snapshots", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].analysisJobId").exists())
                .andReturn();

        Long latestSnapshotId = objectMapper.readTree(snapshotsResult.getResponse().getContentAsString())
                .path("data")
                .path(0)
                .path("id")
                .asLong();

        mockMvc.perform(patch("/api/projects/{projectId}/analysis/snapshots/{snapshotId}", projectId, latestSnapshotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"release-candidate\",\"note\":\"ready for review\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.label").value("release-candidate"))
                .andExpect(jsonPath("$.data.note").value("ready for review"));

        mockMvc.perform(get("/api/projects/{projectId}/analysis/risk-trend", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].releaseRisk.readinessScore").value(100))
                .andExpect(jsonPath("$.data[0].releaseRisk.riskLevel").value("NONE"));

        mockMvc.perform(get("/api/projects/{projectId}/analysis/comparison/latest/report", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.format").value("MARKDOWN"))
                .andExpect(jsonPath("$.data.content", containsString("Analysis Comparison Report")))
                .andExpect(jsonPath("$.data.content", containsString("Change Counts")))
                .andExpect(jsonPath("$.data.content", containsString("Readiness score")));

        mockMvc.perform(get("/api/projects/{projectId}/analysis/comparison/latest/report/download", projectId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Analysis Comparison Report")))
                .andExpect(content().string(containsString("Change Counts")));

        mockMvc.perform(get("/api/projects/{projectId}/analysis/comparison/latest/checklist", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.format").value("MARKDOWN"))
                .andExpect(jsonPath("$.data.content", containsString("Release Review Checklist")))
                .andExpect(jsonPath("$.data.content", containsString("Final Approval")));

        mockMvc.perform(get("/api/projects/{projectId}/analysis/comparison/latest/checklist/download", projectId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Release Review Checklist")))
                .andExpect(content().string(containsString("Final Approval")));

        mockMvc.perform(get("/api/projects/{projectId}/analysis/comparison/latest/release-notes", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.format").value("MARKDOWN"))
                .andExpect(jsonPath("$.data.content", containsString("Release Notes")))
                .andExpect(jsonPath("$.data.content", containsString("Compatibility Notes")));

        mockMvc.perform(get("/api/projects/{projectId}/analysis/comparison/latest/release-notes/download", projectId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Release Notes")))
                .andExpect(content().string(containsString("Compatibility Notes")));

        mockMvc.perform(get("/api/projects/{projectId}/analysis/comparison/latest/github-release-draft", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tagName").exists())
                .andExpect(jsonPath("$.data.releaseName").exists())
                .andExpect(jsonPath("$.data.body", containsString("Release Notes")))
                .andExpect(jsonPath("$.data.draft").value(true));

        mockMvc.perform(post("/api/projects/{projectId}/analysis/comparison/latest/github-release-draft/publish", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.errorMessage", containsString("GitHub publishing is disabled")))
                .andExpect(jsonPath("$.data.baseSnapshotId").exists())
                .andExpect(jsonPath("$.data.targetSnapshotId").exists());

        mockMvc.perform(get("/api/projects/{projectId}/analysis/github-release-publish-history", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status").value("FAILED"))
                .andExpect(jsonPath("$.data[0].errorMessage", containsString("GitHub publishing is disabled")));

        mockMvc.perform(post("/api/projects/{projectId}/questions", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"TB_ORDER 어디서 사용돼?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.confidence").value("HIGH"))
                .andExpect(jsonPath("$.data.relatedApis", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.data.relatedTables[0]").value("TB_ORDER"));

        mockMvc.perform(delete("/api/projects/{projectId}/analysis/results", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedSourceFileCount").value(8))
                .andExpect(jsonPath("$.data.deletedSpringApiCount").value(5))
                .andExpect(jsonPath("$.data.deletedMyBatisStatementCount").value(5))
                .andExpect(jsonPath("$.data.deletedApiFlowCount").value(7));
    }

    private Long createSampleProject() throws Exception {
        String samplePath = Path.of("samples", "legacy-spring-mybatis")
                .toAbsolutePath()
                .normalize()
                .toString()
                .replace("\\", "\\\\");

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "sample-api-contract",
                                  "sourcePath": "%s"
                                }
                                """.formatted(samplePath)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }

    private Long findCreateOrderApiId(Long projectId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/projects/{projectId}/apis", projectId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode apis = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        for (JsonNode api : apis) {
            if (api.path("httpMethod").asText().equals("POST")
                    && api.path("path").asText().equals("/api/orders")) {
                return api.path("id").asLong();
            }
        }
        throw new IllegalStateException("POST /api/orders API was not found.");
    }
}
