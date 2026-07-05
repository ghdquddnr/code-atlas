package com.codeatlas.document.dto;

import java.time.Instant;

public record GeneratedDocumentResponse(
        String title,
        String format,
        String content,
        int evidenceCount,
        Instant generatedAt
) {

    public static GeneratedDocumentResponse markdown(String title, String content, int evidenceCount) {
        return new GeneratedDocumentResponse(title, "MARKDOWN", content, evidenceCount, Instant.now());
    }
}
