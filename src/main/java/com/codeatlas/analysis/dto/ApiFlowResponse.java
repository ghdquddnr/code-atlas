package com.codeatlas.analysis.dto;

import com.codeatlas.analysis.domain.ApiFlow;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record ApiFlowResponse(
        Long id,
        Long projectId,
        String httpMethod,
        String apiPath,
        String controllerClassName,
        String controllerMethodName,
        String serviceClassName,
        String serviceMethodName,
        String mapperNamespace,
        String mapperStatementId,
        String mapperStatementType,
        List<MethodCallStepResponse> methodCallPath,
        Set<String> tableNames,
        Instant mappedAt
) {

    public static ApiFlowResponse from(ApiFlow apiFlow) {
        return new ApiFlowResponse(
                apiFlow.getId(),
                apiFlow.getProject().getId(),
                apiFlow.getHttpMethod(),
                apiFlow.getApiPath(),
                apiFlow.getControllerClassName(),
                apiFlow.getControllerMethodName(),
                apiFlow.getServiceClassName(),
                apiFlow.getServiceMethodName(),
                apiFlow.getMapperNamespace(),
                apiFlow.getMapperStatementId(),
                apiFlow.getMapperStatementType(),
                parseMethodCallPath(apiFlow.getMethodCallPathSummary()),
                new LinkedHashSet<>(apiFlow.getTableNames()),
                apiFlow.getMappedAt()
        );
    }

    private static List<MethodCallStepResponse> parseMethodCallPath(String methodCallPathSummary) {
        if (methodCallPathSummary == null || methodCallPathSummary.isBlank()) {
            return List.of();
        }
        return Arrays.stream(methodCallPathSummary.split("\\n"))
                .filter(line -> !line.isBlank())
                .map(MethodCallStepResponse::parse)
                .toList();
    }
}
