package com.codeatlas.analysis.dto;

import java.util.List;

public record TableUsageDetailResponse(
        String tableName,
        List<MyBatisStatementResponse> statements,
        List<ApiFlowResponse> flows
) {
}
