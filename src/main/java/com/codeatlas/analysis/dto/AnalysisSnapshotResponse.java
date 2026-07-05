package com.codeatlas.analysis.dto;

import java.time.Instant;

public record AnalysisSnapshotResponse(
        Long id,
        Long projectId,
        Long analysisJobId,
        Instant createdAt,
        String label,
        String note
) {
}
