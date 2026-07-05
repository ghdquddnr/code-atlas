package com.codeatlas.analysis.dto;

import java.util.List;

public record ProjectDashboardResponse(
        Long projectId,
        String projectName,
        AnalysisJobResponse latestAnalysisJob,
        long sourceFileCount,
        long springApiCount,
        long myBatisStatementCount,
        long apiFlowCount,
        List<TableUsageResponse> tables
) {
}
