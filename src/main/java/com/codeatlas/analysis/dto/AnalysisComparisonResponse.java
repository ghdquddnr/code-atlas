package com.codeatlas.analysis.dto;

import java.time.Instant;

public record AnalysisComparisonResponse(
        Long projectId,
        Long baseSnapshotId,
        Long targetSnapshotId,
        Long baseJobId,
        Long targetJobId,
        Instant baseCreatedAt,
        Instant targetCreatedAt,
        AnalysisDiffSectionResponse apis,
        AnalysisDiffSectionResponse sqlStatements,
        AnalysisDiffSectionResponse dtos,
        AnalysisDiffSectionResponse flows,
        ReleaseRiskResponse releaseRisk
) {
}
