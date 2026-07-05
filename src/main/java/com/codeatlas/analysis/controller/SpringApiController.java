package com.codeatlas.analysis.controller;

import com.codeatlas.analysis.dto.SpringApiDetailResponse;
import com.codeatlas.analysis.dto.SpringApiResponse;
import com.codeatlas.analysis.service.SpringApiAnalysisService;
import com.codeatlas.common.response.ApiResponse;
import com.codeatlas.project.service.ProjectService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/apis")
public class SpringApiController {

    private final SpringApiAnalysisService springApiAnalysisService;
    private final ProjectService projectService;

    public SpringApiController(SpringApiAnalysisService springApiAnalysisService, ProjectService projectService) {
        this.springApiAnalysisService = springApiAnalysisService;
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<List<SpringApiResponse>> findByProjectId(@PathVariable Long projectId) {
        projectService.findProject(projectId);
        return ApiResponse.ok(springApiAnalysisService.findByProjectId(projectId));
    }

    @GetMapping("/{apiId}")
    public ApiResponse<SpringApiDetailResponse> getDetail(
            @PathVariable Long projectId,
            @PathVariable Long apiId
    ) {
        projectService.findProject(projectId);
        return ApiResponse.ok(springApiAnalysisService.getDetail(projectId, apiId));
    }
}
