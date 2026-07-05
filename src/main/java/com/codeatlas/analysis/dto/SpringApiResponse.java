package com.codeatlas.analysis.dto;

import com.codeatlas.analysis.domain.SpringApi;
import java.time.Instant;

public record SpringApiResponse(
        Long id,
        Long projectId,
        String httpMethod,
        String path,
        String controllerClassName,
        String methodName,
        String requestDtoName,
        String responseDtoName,
        String sourceFilePath,
        Integer lineNumber,
        Instant extractedAt
) {

    public static SpringApiResponse from(SpringApi springApi) {
        return new SpringApiResponse(
                springApi.getId(),
                springApi.getProject().getId(),
                springApi.getHttpMethod(),
                springApi.getPath(),
                springApi.getControllerClassName(),
                springApi.getMethodName(),
                springApi.getRequestDtoName(),
                springApi.getResponseDtoName(),
                springApi.getSourceFilePath(),
                springApi.getLineNumber(),
                springApi.getExtractedAt()
        );
    }
}
