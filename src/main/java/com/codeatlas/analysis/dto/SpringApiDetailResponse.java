package com.codeatlas.analysis.dto;

import java.util.List;

public record SpringApiDetailResponse(
        SpringApiResponse api,
        List<ApiFlowResponse> flows
) {
}
