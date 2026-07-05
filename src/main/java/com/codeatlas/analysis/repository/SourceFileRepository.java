package com.codeatlas.analysis.repository;

import com.codeatlas.analysis.domain.SourceFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceFileRepository extends JpaRepository<SourceFile, Long> {

    List<SourceFile> findByProjectIdOrderByRelativePath(Long projectId);

    long countByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
