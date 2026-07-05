package com.codeatlas.analysis.repository;

import com.codeatlas.analysis.domain.GitHubReleasePublishHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitHubReleasePublishHistoryRepository extends JpaRepository<GitHubReleasePublishHistory, Long> {

    List<GitHubReleasePublishHistory> findTop20ByProjectIdOrderByRequestedAtDesc(Long projectId);
}
