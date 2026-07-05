package com.codeatlas.analysis.service;

import com.codeatlas.analysis.domain.AnalysisJob;
import com.codeatlas.analysis.dto.AnalysisJobResponse;
import com.codeatlas.analysis.repository.AnalysisJobRepository;
import com.codeatlas.project.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisJobStateService {

    private final AnalysisJobRepository analysisJobRepository;

    public AnalysisJobStateService(AnalysisJobRepository analysisJobRepository) {
        this.analysisJobRepository = analysisJobRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AnalysisJobResponse createRunningJob(Project project) {
        AnalysisJob analysisJob = AnalysisJob.create(project);
        analysisJob.markRunning();
        return AnalysisJobResponse.from(analysisJobRepository.save(analysisJob));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AnalysisJobResponse markCompleted(Long jobId, String message) {
        AnalysisJob analysisJob = findJob(jobId);
        analysisJob.markCompleted(message);
        return AnalysisJobResponse.from(analysisJob);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AnalysisJobResponse markFailed(Long jobId, String message) {
        AnalysisJob analysisJob = findJob(jobId);
        analysisJob.markFailed(truncate(message));
        return AnalysisJobResponse.from(analysisJob);
    }

    private AnalysisJob findJob(Long jobId) {
        return analysisJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis job not found: " + jobId));
    }

    private String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "Analysis failed.";
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }
}
