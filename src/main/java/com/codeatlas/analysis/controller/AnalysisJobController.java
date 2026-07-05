package com.codeatlas.analysis.controller;

import com.codeatlas.analysis.dto.AnalysisJobResponse;
import com.codeatlas.analysis.dto.AnalysisResultResetResponse;
import com.codeatlas.analysis.service.AnalysisJobService;
import com.codeatlas.analysis.service.AnalysisResultCleanupService;
import com.codeatlas.common.response.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class AnalysisJobController {

    private final AnalysisJobService analysisJobService;
    private final AnalysisResultCleanupService analysisResultCleanupService;

    public AnalysisJobController(
            AnalysisJobService analysisJobService,
            AnalysisResultCleanupService analysisResultCleanupService
    ) {
        this.analysisJobService = analysisJobService;
        this.analysisResultCleanupService = analysisResultCleanupService;
    }

    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<AnalysisJobResponse> analyze(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisJobService.analyze(projectId));
    }

    @GetMapping("/analysis/status")
    public ApiResponse<AnalysisJobResponse> getLatestStatus(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisJobService.getLatestStatus(projectId));
    }

    @DeleteMapping("/analysis/results")
    public ApiResponse<AnalysisResultResetResponse> resetAnalysisResults(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisResultCleanupService.reset(projectId));
    }
}
