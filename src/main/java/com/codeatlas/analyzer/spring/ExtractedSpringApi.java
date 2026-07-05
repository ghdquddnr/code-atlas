package com.codeatlas.analyzer.spring;

public record ExtractedSpringApi(
        String httpMethod,
        String path,
        String controllerClassName,
        String methodName,
        String requestDtoName,
        String responseDtoName,
        String sourceFilePath,
        Integer lineNumber
) {
}
