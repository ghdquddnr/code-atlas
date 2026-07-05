package com.codeatlas.analysis.repository;

import com.codeatlas.analysis.domain.AnalysisJob;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, Long> {

    Optional<AnalysisJob> findFirstByProjectIdOrderByCreatedAtDesc(Long projectId);

    long countByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
