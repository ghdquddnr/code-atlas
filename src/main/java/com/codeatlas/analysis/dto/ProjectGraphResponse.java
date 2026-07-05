package com.codeatlas.analysis.dto;

import java.util.List;

public record ProjectGraphResponse(
        Long projectId,
        List<GraphNodeResponse> nodes,
        List<GraphEdgeResponse> edges
) {
}
