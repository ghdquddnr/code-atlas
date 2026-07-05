package com.codeatlas.analysis.controller;

import com.codeatlas.analysis.dto.MyBatisStatementResponse;
import com.codeatlas.analysis.dto.TableUsageDetailResponse;
import com.codeatlas.analysis.dto.TableUsageResponse;
import com.codeatlas.analysis.service.MyBatisAnalysisService;
import com.codeatlas.common.response.ApiResponse;
import com.codeatlas.project.service.ProjectService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class MyBatisController {

    private final MyBatisAnalysisService myBatisAnalysisService;
    private final ProjectService projectService;

    public MyBatisController(MyBatisAnalysisService myBatisAnalysisService, ProjectService projectService) {
        this.myBatisAnalysisService = myBatisAnalysisService;
        this.projectService = projectService;
    }

    @GetMapping("/mybatis/statements")
    public ApiResponse<List<MyBatisStatementResponse>> findStatements(@PathVariable Long projectId) {
        projectService.findProject(projectId);
        return ApiResponse.ok(myBatisAnalysisService.findStatements(projectId));
    }

    @GetMapping("/tables")
    public ApiResponse<List<TableUsageResponse>> findTables(@PathVariable Long projectId) {
        projectService.findProject(projectId);
        return ApiResponse.ok(myBatisAnalysisService.findTables(projectId));
    }

    @GetMapping("/tables/{tableName}")
    public ApiResponse<TableUsageDetailResponse> getTableUsageDetail(
            @PathVariable Long projectId,
            @PathVariable String tableName
    ) {
        projectService.findProject(projectId);
        return ApiResponse.ok(myBatisAnalysisService.getTableUsageDetail(projectId, tableName));
    }
}
