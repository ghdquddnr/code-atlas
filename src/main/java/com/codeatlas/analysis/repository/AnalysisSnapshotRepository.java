package com.codeatlas.analysis.repository;

import com.codeatlas.analysis.domain.AnalysisSnapshot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisSnapshotRepository extends JpaRepository<AnalysisSnapshot, Long> {

    List<AnalysisSnapshot> findTop2ByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<AnalysisSnapshot> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    Optional<AnalysisSnapshot> findByIdAndProjectId(Long id, Long projectId);

    long countByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
