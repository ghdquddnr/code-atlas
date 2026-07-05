package com.codeatlas.analysis.dto;

public record GitHubReleaseDraftResponse(
        String tagName,
        String releaseName,
        String body,
        boolean draft,
        boolean prerelease
) {
}
