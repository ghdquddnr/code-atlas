package com.codeatlas.analysis.dto;

import com.codeatlas.analysis.domain.SourceFile;
import com.codeatlas.analysis.domain.SourceFileType;
import com.codeatlas.analyzer.java.JavaClassCategory;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record SourceFileResponse(
        Long id,
        Long projectId,
        String path,
        String relativePath,
        SourceFileType type,
        String packageName,
        String className,
        JavaClassCategory classCategory,
        List<JavaClassFieldResponse> classFields,
        long sizeBytes,
        Instant indexedAt
) {

    public static SourceFileResponse from(SourceFile sourceFile) {
        return new SourceFileResponse(
                sourceFile.getId(),
                sourceFile.getProject().getId(),
                sourceFile.getPath(),
                sourceFile.getRelativePath(),
                sourceFile.getType(),
                sourceFile.getPackageName(),
                sourceFile.getClassName(),
                sourceFile.getClassCategory(),
                parseFields(sourceFile.getClassFieldSummary()),
                sourceFile.getSizeBytes(),
                sourceFile.getIndexedAt()
        );
    }

    private static List<JavaClassFieldResponse> parseFields(String classFieldSummary) {
        if (classFieldSummary == null || classFieldSummary.isBlank()) {
            return List.of();
        }
        return Arrays.stream(classFieldSummary.split("\\n"))
                .map(line -> line.split(":", 2))
                .filter(parts -> parts.length == 2)
                .map(parts -> new JavaClassFieldResponse(parts[0], parts[1]))
                .toList();
    }
}
