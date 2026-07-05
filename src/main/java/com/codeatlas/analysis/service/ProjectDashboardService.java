package com.codeatlas.analysis.service;

import com.codeatlas.analysis.dto.AnalysisJobResponse;
import com.codeatlas.analysis.dto.ProjectDashboardResponse;
import com.codeatlas.analysis.repository.AnalysisJobRepository;
import com.codeatlas.analysis.repository.ApiFlowRepository;
import com.codeatlas.analysis.repository.MyBatisStatementRepository;
import com.codeatlas.analysis.repository.SourceFileRepository;
import com.codeatlas.analysis.repository.SpringApiRepository;
import com.codeatlas.project.domain.Project;
import com.codeatlas.project.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectDashboardService {

    private final ProjectService projectService;
    private final AnalysisJobRepository analysisJobRepository;
    private final SourceFileRepository sourceFileRepository;
    private final SpringApiRepository springApiRepository;
    private final MyBatisStatementRepository myBatisStatementRepository;
    private final ApiFlowRepository apiFlowRepository;
    private final MyBatisAnalysisService myBatisAnalysisService;

    public ProjectDashboardService(
            ProjectService projectService,
            AnalysisJobRepository analysisJobRepository,
            SourceFileRepository sourceFileRepository,
            SpringApiRepository springApiRepository,
            MyBatisStatementRepository myBatisStatementRepository,
            ApiFlowRepository apiFlowRepository,
            MyBatisAnalysisService myBatisAnalysisService
    ) {
        this.projectService = projectService;
        this.analysisJobRepository = analysisJobRepository;
        this.sourceFileRepository = sourceFileRepository;
        this.springApiRepository = springApiRepository;
        this.myBatisStatementRepository = myBatisStatementRepository;
        this.apiFlowRepository = apiFlowRepository;
        this.myBatisAnalysisService = myBatisAnalysisService;
    }

    @Transactional(readOnly = true)
    public ProjectDashboardResponse getDashboard(Long projectId) {
        Project project = projectService.findProject(projectId);
        AnalysisJobResponse latestJob = analysisJobRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .map(AnalysisJobResponse::from)
                .orElse(null);

        return new ProjectDashboardResponse(
                project.getId(),
                project.getName(),
                latestJob,
                sourceFileRepository.countByProjectId(projectId),
                springApiRepository.countByProjectId(projectId),
                myBatisStatementRepository.countByProjectId(projectId),
                apiFlowRepository.countByProjectId(projectId),
                myBatisAnalysisService.findTables(projectId)
        );
    }
}
