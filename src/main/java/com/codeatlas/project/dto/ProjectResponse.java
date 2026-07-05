package com.codeatlas.project.dto;

import com.codeatlas.project.domain.Project;
import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        String sourcePath,
        Instant createdAt
) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getSourcePath(),
                project.getCreatedAt()
        );
    }
}
