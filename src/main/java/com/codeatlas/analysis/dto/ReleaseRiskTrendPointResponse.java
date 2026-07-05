package com.codeatlas.analysis.dto;

import java.time.Instant;

public record ReleaseRiskTrendPointResponse(
        Long baseSnapshotId,
        Long targetSnapshotId,
        Long baseJobId,
        Long targetJobId,
        Instant targetCreatedAt,
        ReleaseRiskResponse releaseRisk,
        int totalChangeCount
) {
}
