package com.codeatlas.project.service;

import com.codeatlas.analysis.dto.AnalysisResultResetResponse;
import com.codeatlas.analysis.repository.AnalysisJobRepository;
import com.codeatlas.analysis.repository.AnalysisSnapshotRepository;
import com.codeatlas.analysis.service.AnalysisResultCleanupService;
import com.codeatlas.project.dto.ProjectDeleteResponse;
import com.codeatlas.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectDeletionService {

    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final AnalysisResultCleanupService analysisResultCleanupService;
    private final AnalysisJobRepository analysisJobRepository;
    private final AnalysisSnapshotRepository analysisSnapshotRepository;

    public ProjectDeletionService(
            ProjectService projectService,
            ProjectRepository projectRepository,
            AnalysisResultCleanupService analysisResultCleanupService,
            AnalysisJobRepository analysisJobRepository,
            AnalysisSnapshotRepository analysisSnapshotRepository
    ) {
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.analysisResultCleanupService = analysisResultCleanupService;
        this.analysisJobRepository = analysisJobRepository;
        this.analysisSnapshotRepository = analysisSnapshotRepository;
    }

    @Transactional
    public ProjectDeleteResponse delete(Long projectId) {
        projectService.findProject(projectId);

        AnalysisResultResetResponse reset = analysisResultCleanupService.reset(projectId);
        long analysisJobCount = analysisJobRepository.countByProjectId(projectId);
        analysisSnapshotRepository.deleteByProjectId(projectId);
        analysisJobRepository.deleteByProjectId(projectId);
        projectRepository.deleteById(projectId);

        return new ProjectDeleteResponse(
                projectId,
                reset.deletedSourceFileCount(),
                reset.deletedSpringApiCount(),
                reset.deletedMyBatisStatementCount(),
                reset.deletedApiFlowCount(),
                analysisJobCount
        );
    }
}
