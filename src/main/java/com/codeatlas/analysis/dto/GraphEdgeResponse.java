package com.codeatlas.analysis.dto;

public record GraphEdgeResponse(
        String id,
        String source,
        String target,
        String label
) {
}
