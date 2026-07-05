package com.codeatlas.analysis.controller;

import com.codeatlas.analysis.dto.ProjectDashboardResponse;
import com.codeatlas.analysis.service.ProjectDashboardService;
import com.codeatlas.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/dashboard")
public class ProjectDashboardController {

    private final ProjectDashboardService projectDashboardService;

    public ProjectDashboardController(ProjectDashboardService projectDashboardService) {
        this.projectDashboardService = projectDashboardService;
    }

    @GetMapping
    public ApiResponse<ProjectDashboardResponse> getDashboard(@PathVariable Long projectId) {
        return ApiResponse.ok(projectDashboardService.getDashboard(projectId));
    }
}
