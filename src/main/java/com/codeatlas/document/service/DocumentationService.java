package com.codeatlas.document.service;

import com.codeatlas.ai.LocalLlmClient;
import com.codeatlas.analysis.dto.ApiFlowResponse;
import com.codeatlas.analysis.dto.MyBatisStatementResponse;
import com.codeatlas.analysis.dto.SpringApiResponse;
import com.codeatlas.analysis.dto.TableUsageResponse;
import com.codeatlas.analysis.service.ApiFlowMappingService;
import com.codeatlas.analysis.service.MyBatisAnalysisService;
import com.codeatlas.analysis.service.SpringApiAnalysisService;
import com.codeatlas.document.dto.GeneratedDocumentResponse;
import com.codeatlas.project.dto.ProjectResponse;
import com.codeatlas.project.service.ProjectService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentationService {

    private final ProjectService projectService;
    private final SpringApiAnalysisService springApiAnalysisService;
    private final MyBatisAnalysisService myBatisAnalysisService;
    private final ApiFlowMappingService apiFlowMappingService;
    private final LocalLlmClient localLlmClient;

    public DocumentationService(
            ProjectService projectService,
            SpringApiAnalysisService springApiAnalysisService,
            MyBatisAnalysisService myBatisAnalysisService,
            ApiFlowMappingService apiFlowMappingService,
            LocalLlmClient localLlmClient
    ) {
        this.projectService = projectService;
        this.springApiAnalysisService = springApiAnalysisService;
        this.myBatisAnalysisService = myBatisAnalysisService;
        this.apiFlowMappingService = apiFlowMappingService;
        this.localLlmClient = localLlmClient;
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse generateOnboardingDocument(Long projectId) {
        ProjectResponse project = projectService.getById(projectId);
        List<SpringApiResponse> apis = springApiAnalysisService.findByProjectId(projectId);
        List<MyBatisStatementResponse> statements = myBatisAnalysisService.findStatements(projectId);
        List<TableUsageResponse> tables = myBatisAnalysisService.findTables(projectId);
        List<ApiFlowResponse> flows = apiFlowMappingService.findByProjectId(projectId);

        String content = """
                # %s Onboarding Guide

                ## Project Summary

                - Project ID: %d
                - Source path: `%s`
                - Indexed Spring APIs: %d
                - MyBatis statements: %d
                - Tables: %d
                - API flows: %d

                ## Main Entry Points

                %s

                ## Table Usage Summary

                %s

                ## Flow Evidence

                %s

                ## Evidence Policy

                This document is generated only from deterministic analysis results stored by CodeAtlas.
                """.formatted(
                project.name(),
                project.id(),
                project.sourcePath(),
                apis.size(),
                statements.size(),
                tables.size(),
                flows.size(),
                formatApiList(apis),
                formatTableList(tables),
                formatFlowList(flows)
        );

        String enhancedContent = enhanceDocument("onboarding guide", content)
                .orElse(content);

        return GeneratedDocumentResponse.markdown(project.name() + " Onboarding Guide", enhancedContent, evidenceCount(apis, statements, flows));
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse generateApiDocument(Long projectId) {
        ProjectResponse project = projectService.getById(projectId);
        List<SpringApiResponse> apis = springApiAnalysisService.findByProjectId(projectId);
        List<ApiFlowResponse> flows = apiFlowMappingService.findByProjectId(projectId);

        String content = """
                # %s API Documentation

                ## APIs

                %s

                ## API Flow Details

                %s

                ## Evidence Policy

                Each API entry is based on Spring controller annotations and each flow row is based on direct Java method calls and matched MyBatis statement IDs.
                """.formatted(project.name(), formatApiList(apis), formatFlowList(flows));

        String enhancedContent = enhanceDocument("API documentation", content)
                .orElse(content);

        return GeneratedDocumentResponse.markdown(project.name() + " API Documentation", enhancedContent, evidenceCount(apis, List.of(), flows));
    }

    private int evidenceCount(
            List<SpringApiResponse> apis,
            List<MyBatisStatementResponse> statements,
            List<ApiFlowResponse> flows
    ) {
        return apis.size() + statements.size() + flows.size();
    }

    private String formatApiList(List<SpringApiResponse> apis) {
        if (apis.isEmpty()) {
            return "No Spring APIs have been extracted.";
        }
        return apis.stream()
                .sorted(Comparator.comparing(SpringApiResponse::path).thenComparing(SpringApiResponse::httpMethod))
                .map(api -> "- `%s %s` -> `%s.%s()` (%s:%s)".formatted(
                        api.httpMethod(),
                        api.path(),
                        api.controllerClassName(),
                        api.methodName(),
                        api.sourceFilePath(),
                        api.lineNumber() == null ? "unknown" : api.lineNumber()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatTableList(List<TableUsageResponse> tables) {
        if (tables.isEmpty()) {
            return "No table usage has been extracted.";
        }
        return tables.stream()
                .map(table -> "- `%s`: %d SQL statement(s)".formatted(table.tableName(), table.statementCount()))
                .collect(Collectors.joining("\n"));
    }

    private String formatFlowList(List<ApiFlowResponse> flows) {
        if (flows.isEmpty()) {
            return "No API flows have been mapped.";
        }
        return flows.stream()
                .map(flow -> "- `%s %s` -> `%s.%s()` -> `%s.%s` -> tables %s".formatted(
                        flow.httpMethod(),
                        flow.apiPath(),
                        flow.serviceClassName(),
                        flow.serviceMethodName(),
                        flow.mapperNamespace(),
                        flow.mapperStatementId(),
                        flow.tableNames()
                ))
                .collect(Collectors.joining("\n"));
    }

    private java.util.Optional<String> enhanceDocument(String documentKind, String deterministicContent) {
        return localLlmClient.generate("""
                You are CodeAtlas, an evidence-based documentation assistant for Java/Spring/MyBatis legacy systems.
                Write in Korean.

                Task:
                Rewrite the deterministic analysis facts below into a practical %s for a new or maintenance developer.

                Rules:
                - Use only the facts in the deterministic document.
                - Do not invent modules, files, classes, APIs, SQL, tables, risks, or architecture.
                - Preserve concrete evidence references such as API paths, class names, method names, Mapper IDs, and table names.
                - If a fact is not present, do not add it.
                - Include a short "근거 정책" section explaining that the document is based on extracted facts.

                Deterministic document:
                %s
                """.formatted(documentKind, deterministicContent))
                .map(content -> content + "\n\n---\n\n## Deterministic Evidence Appendix\n\n" + deterministicContent);
    }
}
