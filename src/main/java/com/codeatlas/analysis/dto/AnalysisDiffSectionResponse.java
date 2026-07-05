package com.codeatlas.analysis.dto;

import java.util.List;

public record AnalysisDiffSectionResponse(
        int addedCount,
        int removedCount,
        List<String> added,
        List<String> removed
) {
}
