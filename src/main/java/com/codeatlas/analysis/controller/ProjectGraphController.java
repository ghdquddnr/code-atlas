package com.codeatlas.analysis.controller;

import com.codeatlas.analysis.dto.ProjectGraphResponse;
import com.codeatlas.analysis.service.ProjectGraphService;
import com.codeatlas.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/graph")
public class ProjectGraphController {

    private final ProjectGraphService projectGraphService;

    public ProjectGraphController(ProjectGraphService projectGraphService) {
        this.projectGraphService = projectGraphService;
    }

    @GetMapping
    public ApiResponse<ProjectGraphResponse> getGraph(@PathVariable Long projectId) {
        return ApiResponse.ok(projectGraphService.getGraph(projectId));
    }
}
