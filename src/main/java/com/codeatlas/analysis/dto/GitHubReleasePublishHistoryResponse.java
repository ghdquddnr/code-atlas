package com.codeatlas.analysis.dto;

import com.codeatlas.analysis.domain.GitHubReleasePublishStatus;
import java.time.Instant;

public record GitHubReleasePublishHistoryResponse(
        Long id,
        Long projectId,
        Long baseSnapshotId,
        Long targetSnapshotId,
        Instant requestedAt,
        Instant completedAt,
        GitHubReleasePublishStatus status,
        String tagName,
        String releaseName,
        String releaseId,
        String htmlUrl,
        String apiUrl,
        boolean draft,
        boolean prerelease,
        String errorMessage
) {
}
