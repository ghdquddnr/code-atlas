package com.codeatlas.chat.service;

import com.codeatlas.ai.LocalLlmClient;
import com.codeatlas.analysis.dto.ApiFlowResponse;
import com.codeatlas.analysis.dto.TableUsageDetailResponse;
import com.codeatlas.analysis.dto.TableUsageResponse;
import com.codeatlas.analysis.service.ApiFlowMappingService;
import com.codeatlas.analysis.service.MyBatisAnalysisService;
import com.codeatlas.chat.dto.QuestionAnswerResponse;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionAnswerService {

    private static final Pattern TABLE_PATTERN = Pattern.compile("\\b[A-Z][A-Z0-9_]*\\b");

    private final ApiFlowMappingService apiFlowMappingService;
    private final MyBatisAnalysisService myBatisAnalysisService;
    private final LocalLlmClient localLlmClient;

    public QuestionAnswerService(
            ApiFlowMappingService apiFlowMappingService,
            MyBatisAnalysisService myBatisAnalysisService,
            LocalLlmClient localLlmClient
    ) {
        this.apiFlowMappingService = apiFlowMappingService;
        this.myBatisAnalysisService = myBatisAnalysisService;
        this.localLlmClient = localLlmClient;
    }

    @Transactional(readOnly = true)
    public QuestionAnswerResponse answer(Long projectId, String question) {
        List<ApiFlowResponse> allFlows = apiFlowMappingService.findByProjectId(projectId);
        List<TableUsageResponse> tables = myBatisAnalysisService.findTables(projectId);
        List<ApiFlowResponse> matchedFlows = findMatchedFlows(question, allFlows, tables, projectId);

        if (matchedFlows.isEmpty()) {
            return new QuestionAnswerResponse(
                    question,
                    "분석된 API Flow와 테이블 사용처에서 질문과 직접 연결되는 근거를 찾지 못했습니다.",
                    "LOW",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of("No matching API flow, mapper statement, or table usage was found."),
                    Instant.now()
            );
        }

        Set<String> relatedApis = new LinkedHashSet<>();
        Set<String> relatedMapperStatements = new LinkedHashSet<>();
        Set<String> relatedTables = new LinkedHashSet<>();
        Set<String> evidence = new LinkedHashSet<>();

        for (ApiFlowResponse flow : matchedFlows) {
            relatedApis.add(flow.httpMethod() + " " + flow.apiPath());
            relatedMapperStatements.add(flow.mapperNamespace() + "." + flow.mapperStatementId());
            relatedTables.addAll(flow.tableNames());
            evidence.add("%s %s -> %s.%s() -> %s.%s -> %s".formatted(
                    flow.httpMethod(),
                    flow.apiPath(),
                    flow.serviceClassName(),
                    flow.serviceMethodName(),
                    flow.mapperNamespace(),
                    flow.mapperStatementId(),
                    flow.tableNames()
            ));
        }

        String deterministicAnswer = buildAnswer(relatedApis, relatedMapperStatements, relatedTables);
        String answer = localLlmClient.generate(buildPrompt(
                        question,
                        relatedApis,
                        relatedMapperStatements,
                        relatedTables,
                        evidence
                ))
                .orElse(deterministicAnswer);
        String confidence = matchedFlows.size() >= 2 ? "HIGH" : "MEDIUM";

        return new QuestionAnswerResponse(
                question,
                answer,
                confidence,
                List.copyOf(relatedApis),
                List.copyOf(relatedMapperStatements),
                List.copyOf(relatedTables),
                List.copyOf(evidence),
                Instant.now()
        );
    }

    private List<ApiFlowResponse> findMatchedFlows(
            String question,
            List<ApiFlowResponse> allFlows,
            List<TableUsageResponse> tables,
            Long projectId
    ) {
        String normalizedQuestion = question.toUpperCase(Locale.ROOT);
        Set<String> matchedTableNames = findMatchedTables(normalizedQuestion, tables);

        if (!matchedTableNames.isEmpty()) {
            return matchedTableNames.stream()
                    .flatMap(tableName -> {
                        TableUsageDetailResponse detail = myBatisAnalysisService.getTableUsageDetail(projectId, tableName);
                        return detail.flows().stream();
                    })
                    .sorted(flowComparator())
                    .toList();
        }

        return allFlows.stream()
                .filter(flow -> matchesApiOrMethod(normalizedQuestion, flow))
                .sorted(flowComparator())
                .toList();
    }

    private Set<String> findMatchedTables(String normalizedQuestion, List<TableUsageResponse> tables) {
        Set<String> tableNames = new LinkedHashSet<>();
        var matcher = TABLE_PATTERN.matcher(normalizedQuestion);
        while (matcher.find()) {
            String token = matcher.group();
            tables.stream()
                    .map(TableUsageResponse::tableName)
                    .filter(tableName -> tableName.equals(token))
                    .findFirst()
                    .ifPresent(tableNames::add);
        }
        return tableNames;
    }

    private boolean matchesApiOrMethod(String normalizedQuestion, ApiFlowResponse flow) {
        String searchable = String.join(" ",
                flow.httpMethod(),
                flow.apiPath(),
                flow.controllerMethodName(),
                flow.serviceMethodName(),
                flow.mapperStatementId()
        ).toUpperCase(Locale.ROOT);

        return splitQuestion(normalizedQuestion).stream()
                .filter(token -> token.length() >= 4)
                .anyMatch(searchable::contains);
    }

    private Set<String> splitQuestion(String normalizedQuestion) {
        String[] tokens = normalizedQuestion.split("[^A-Z0-9_가-힣]+");
        Set<String> result = new LinkedHashSet<>();
        for (String token : tokens) {
            if (!token.isBlank()) {
                result.add(token);
            }
        }
        return result;
    }

    private Comparator<ApiFlowResponse> flowComparator() {
        return Comparator.comparing(ApiFlowResponse::apiPath)
                .thenComparing(ApiFlowResponse::httpMethod)
                .thenComparing(ApiFlowResponse::mapperStatementId);
    }

    private String buildAnswer(Set<String> apis, Set<String> mapperStatements, Set<String> tables) {
        return """
                분석된 근거 기준으로 관련 흐름을 찾았습니다.
                관련 API는 %s 입니다.
                관련 Mapper/SQL은 %s 입니다.
                관련 테이블은 %s 입니다.
                """.formatted(apis, mapperStatements, tables).trim();
    }

    private String buildPrompt(
            String question,
            Set<String> apis,
            Set<String> mapperStatements,
            Set<String> tables,
            Set<String> evidence
    ) {
        return """
                You are CodeAtlas, an evidence-based assistant for Java/Spring/MyBatis legacy code analysis.
                Answer in Korean.

                Rules:
                - Use only the evidence below.
                - Do not invent files, classes, methods, SQL, tables, or business meaning.
                - If evidence is limited, say that the answer is based only on extracted flow facts.
                - Start with the direct answer.
                - Include sections: 관련 API, 관련 Mapper/SQL, 관련 테이블, 근거.

                Question:
                %s

                Related APIs:
                %s

                Related Mapper/SQL:
                %s

                Related Tables:
                %s

                Evidence:
                %s
                """.formatted(question, apis, mapperStatements, tables, evidence);
    }
}
