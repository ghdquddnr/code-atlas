package com.codeatlas.project.dto;

public record ProjectDeleteResponse(
        Long projectId,
        long deletedSourceFileCount,
        long deletedSpringApiCount,
        long deletedMyBatisStatementCount,
        long deletedApiFlowCount,
        long deletedAnalysisJobCount
) {
}
