package com.codeatlas.analysis.controller;

import com.codeatlas.analysis.dto.SourceFileResponse;
import com.codeatlas.analysis.service.SourceFileIndexingService;
import com.codeatlas.common.response.ApiResponse;
import com.codeatlas.project.service.ProjectService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/source-files")
public class SourceFileController {

    private final SourceFileIndexingService sourceFileIndexingService;
    private final ProjectService projectService;

    public SourceFileController(SourceFileIndexingService sourceFileIndexingService, ProjectService projectService) {
        this.sourceFileIndexingService = sourceFileIndexingService;
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<List<SourceFileResponse>> findByProjectId(@PathVariable Long projectId) {
        projectService.findProject(projectId);
        return ApiResponse.ok(sourceFileIndexingService.findByProjectId(projectId));
    }
}
