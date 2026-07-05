package com.codeatlas.analysis.dto;

import com.codeatlas.analysis.domain.MyBatisStatement;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public record MyBatisStatementResponse(
        Long id,
        Long projectId,
        String namespace,
        String statementId,
        String statementType,
        String sql,
        Set<String> tableNames,
        String parameterType,
        String resultType,
        String sourceFilePath,
        Instant extractedAt
) {

    public static MyBatisStatementResponse from(MyBatisStatement statement) {
        return new MyBatisStatementResponse(
                statement.getId(),
                statement.getProject().getId(),
                statement.getNamespace(),
                statement.getStatementId(),
                statement.getStatementType(),
                statement.getSql(),
                new LinkedHashSet<>(statement.getTableNames()),
                statement.getParameterType(),
                statement.getResultType(),
                statement.getSourceFilePath(),
                statement.getExtractedAt()
        );
    }
}
