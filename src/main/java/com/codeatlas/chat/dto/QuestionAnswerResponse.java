package com.codeatlas.chat.dto;

import java.time.Instant;
import java.util.List;

public record QuestionAnswerResponse(
        String question,
        String answer,
        String confidence,
        List<String> relatedApis,
        List<String> relatedMapperStatements,
        List<String> relatedTables,
        List<String> evidence,
        Instant answeredAt
) {
}
