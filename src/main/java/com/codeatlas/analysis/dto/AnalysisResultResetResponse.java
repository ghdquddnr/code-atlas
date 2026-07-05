package com.codeatlas.analysis.dto;

public record AnalysisResultResetResponse(
        Long projectId,
        long deletedSourceFileCount,
        long deletedSpringApiCount,
        long deletedMyBatisStatementCount,
        long deletedApiFlowCount
) {
}
