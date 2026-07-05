package com.codeatlas.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required.")
        @Size(max = 120, message = "Project name must be 120 characters or fewer.")
        String name,

        @NotBlank(message = "Source path is required.")
        @Size(max = 1000, message = "Source path must be 1000 characters or fewer.")
        String sourcePath
) {
}
