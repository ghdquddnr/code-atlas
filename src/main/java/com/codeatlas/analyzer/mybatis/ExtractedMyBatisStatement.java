package com.codeatlas.analyzer.mybatis;

import java.util.Set;

public record ExtractedMyBatisStatement(
        String namespace,
        String statementId,
        String statementType,
        String sql,
        Set<String> tableNames,
        String parameterType,
        String resultType,
        String sourceFilePath
) {
}
