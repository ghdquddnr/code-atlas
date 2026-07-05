package com.codeatlas.analysis.repository;

import com.codeatlas.analysis.domain.SpringApi;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringApiRepository extends JpaRepository<SpringApi, Long> {

    List<SpringApi> findByProjectIdOrderByPathAscHttpMethodAsc(Long projectId);

    long countByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
