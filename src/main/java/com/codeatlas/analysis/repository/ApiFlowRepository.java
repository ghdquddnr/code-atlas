package com.codeatlas.analysis.repository;

import com.codeatlas.analysis.domain.ApiFlow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiFlowRepository extends JpaRepository<ApiFlow, Long> {

    List<ApiFlow> findByProjectIdOrderByApiPathAscHttpMethodAscMapperStatementIdAsc(Long projectId);

    long countByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
