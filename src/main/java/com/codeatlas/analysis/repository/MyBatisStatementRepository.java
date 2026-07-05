package com.codeatlas.analysis.repository;

import com.codeatlas.analysis.domain.MyBatisStatement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyBatisStatementRepository extends JpaRepository<MyBatisStatement, Long> {

    List<MyBatisStatement> findByProjectIdOrderByNamespaceAscStatementIdAsc(Long projectId);

    long countByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
