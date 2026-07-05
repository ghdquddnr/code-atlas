package com.codeatlas.analysis.dto;

public record TableUsageResponse(
        String tableName,
        long statementCount
) {
}
