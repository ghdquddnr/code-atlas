package com.codeatlas.analyzer.java;

public record ExtractedMethodCall(
        String ownerClassName,
        String callerMethodName,
        String targetFieldName,
        String targetClassName,
        String targetMethodName
) {
}
