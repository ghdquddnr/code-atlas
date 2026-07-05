package com.codeatlas.analysis.dto;

public record GitHubReleasePublishResponse(
        String id,
        String tagName,
        String releaseName,
        String htmlUrl,
        String apiUrl,
        boolean draft,
        boolean prerelease
) {
}
