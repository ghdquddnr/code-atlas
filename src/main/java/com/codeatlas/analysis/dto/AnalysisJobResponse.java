package com.codeatlas.analysis.dto;

import com.codeatlas.analysis.domain.AnalysisJob;
import com.codeatlas.analysis.domain.AnalysisJobStatus;
import java.time.Instant;

public record AnalysisJobResponse(
        Long id,
        Long projectId,
        AnalysisJobStatus status,
        String message,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt
) {

    public static AnalysisJobResponse from(AnalysisJob analysisJob) {
        return new AnalysisJobResponse(
                analysisJob.getId(),
                analysisJob.getProject().getId(),
                analysisJob.getStatus(),
                analysisJob.getMessage(),
                analysisJob.getCreatedAt(),
                analysisJob.getStartedAt(),
                analysisJob.getFinishedAt()
        );
    }
}
