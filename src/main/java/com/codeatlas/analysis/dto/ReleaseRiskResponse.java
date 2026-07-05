package com.codeatlas.analysis.dto;

public record ReleaseRiskResponse(
        int readinessScore,
        String riskLevel,
        String riskReason
) {
}
