package com.codeatlas.analysis.service;

import com.codeatlas.analysis.dto.AnalysisResultResetResponse;
import com.codeatlas.analysis.repository.ApiFlowRepository;
import com.codeatlas.analysis.repository.MyBatisStatementRepository;
import com.codeatlas.analysis.repository.SourceFileRepository;
import com.codeatlas.analysis.repository.SpringApiRepository;
import com.codeatlas.project.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisResultCleanupService {

    private final ProjectService projectService;
    private final ApiFlowRepository apiFlowRepository;
    private final SpringApiRepository springApiRepository;
    private final MyBatisStatementRepository myBatisStatementRepository;
    private final SourceFileRepository sourceFileRepository;

    public AnalysisResultCleanupService(
            ProjectService projectService,
            ApiFlowRepository apiFlowRepository,
            SpringApiRepository springApiRepository,
            MyBatisStatementRepository myBatisStatementRepository,
            SourceFileRepository sourceFileRepository
    ) {
        this.projectService = projectService;
        this.apiFlowRepository = apiFlowRepository;
        this.springApiRepository = springApiRepository;
        this.myBatisStatementRepository = myBatisStatementRepository;
        this.sourceFileRepository = sourceFileRepository;
    }

    @Transactional
    public AnalysisResultResetResponse reset(Long projectId) {
        projectService.findProject(projectId);

        long apiFlowCount = apiFlowRepository.countByProjectId(projectId);
        long springApiCount = springApiRepository.countByProjectId(projectId);
        long myBatisStatementCount = myBatisStatementRepository.countByProjectId(projectId);
        long sourceFileCount = sourceFileRepository.countByProjectId(projectId);

        apiFlowRepository.deleteByProjectId(projectId);
        springApiRepository.deleteByProjectId(projectId);
        myBatisStatementRepository.deleteByProjectId(projectId);
        sourceFileRepository.deleteByProjectId(projectId);

        return new AnalysisResultResetResponse(
                projectId,
                sourceFileCount,
                springApiCount,
                myBatisStatementCount,
                apiFlowCount
        );
    }
}
