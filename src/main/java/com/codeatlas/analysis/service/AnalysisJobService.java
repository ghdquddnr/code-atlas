package com.codeatlas.analysis.service;

import com.codeatlas.analysis.domain.AnalysisJob;
import com.codeatlas.analysis.dto.AnalysisJobResponse;
import com.codeatlas.analysis.repository.AnalysisJobRepository;
import com.codeatlas.project.domain.Project;
import com.codeatlas.project.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisJobService {

    private final AnalysisJobRepository analysisJobRepository;
    private final ProjectService projectService;
    private final AnalysisJobStateService analysisJobStateService;
    private final SourceFileIndexingService sourceFileIndexingService;
    private final SpringApiAnalysisService springApiAnalysisService;
    private final MyBatisAnalysisService myBatisAnalysisService;
    private final ApiFlowMappingService apiFlowMappingService;
    private final AnalysisSnapshotService analysisSnapshotService;

    public AnalysisJobService(
            AnalysisJobRepository analysisJobRepository,
            ProjectService projectService,
            AnalysisJobStateService analysisJobStateService,
            SourceFileIndexingService sourceFileIndexingService,
            SpringApiAnalysisService springApiAnalysisService,
            MyBatisAnalysisService myBatisAnalysisService,
            ApiFlowMappingService apiFlowMappingService,
            AnalysisSnapshotService analysisSnapshotService
    ) {
        this.analysisJobRepository = analysisJobRepository;
        this.projectService = projectService;
        this.analysisJobStateService = analysisJobStateService;
        this.sourceFileIndexingService = sourceFileIndexingService;
        this.springApiAnalysisService = springApiAnalysisService;
        this.myBatisAnalysisService = myBatisAnalysisService;
        this.apiFlowMappingService = apiFlowMappingService;
        this.analysisSnapshotService = analysisSnapshotService;
    }

    public AnalysisJobResponse analyze(Long projectId) {
        Project project = projectService.findProject(projectId);
        AnalysisJobResponse runningJob = analysisJobStateService.createRunningJob(project);

        try {
            long indexedFileCount = sourceFileIndexingService.index(project);
            long springApiCount = springApiAnalysisService.extract(project);
            long myBatisStatementCount = myBatisAnalysisService.extract(project);
            long apiFlowCount = apiFlowMappingService.map(project);
            AnalysisJobResponse completedJob = analysisJobStateService.markCompleted(
                    runningJob.id(),
                    "Indexed " + indexedFileCount + " source files and extracted "
                            + springApiCount + " Spring APIs and "
                            + myBatisStatementCount + " MyBatis statements and "
                            + apiFlowCount + " API flows."
            );
            analysisSnapshotService.capture(project, completedJob.id());
            return completedJob;
        } catch (RuntimeException exception) {
            return analysisJobStateService.markFailed(runningJob.id(), exception.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public AnalysisJobResponse getLatestStatus(Long projectId) {
        projectService.findProject(projectId);
        return analysisJobRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .map(AnalysisJobResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Analysis job not found for project: " + projectId));
    }
}
