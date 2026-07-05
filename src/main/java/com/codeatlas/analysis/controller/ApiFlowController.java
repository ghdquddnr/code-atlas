package com.codeatlas.analysis.controller;

import com.codeatlas.analysis.dto.ApiFlowResponse;
import com.codeatlas.analysis.service.ApiFlowMappingService;
import com.codeatlas.common.response.ApiResponse;
import com.codeatlas.project.service.ProjectService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/flows")
public class ApiFlowController {

    private final ApiFlowMappingService apiFlowMappingService;
    private final ProjectService projectService;

    public ApiFlowController(ApiFlowMappingService apiFlowMappingService, ProjectService projectService) {
        this.apiFlowMappingService = apiFlowMappingService;
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<List<ApiFlowResponse>> findByProjectId(@PathVariable Long projectId) {
        projectService.findProject(projectId);
        return ApiResponse.ok(apiFlowMappingService.findByProjectId(projectId));
    }
}
