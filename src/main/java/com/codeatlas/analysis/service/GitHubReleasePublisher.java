package com.codeatlas.analysis.service;

import com.codeatlas.analysis.dto.GitHubReleaseDraftResponse;
import com.codeatlas.analysis.dto.GitHubReleasePublishResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class GitHubReleasePublisher {

    private final RestClient restClient;
    private final boolean enabled;
    private final String token;
    private final String owner;
    private final String repository;

    public GitHubReleasePublisher(
            RestClient.Builder restClientBuilder,
            @Value("${code-atlas.github.enabled:false}") boolean enabled,
            @Value("${code-atlas.github.base-url:https://api.github.com}") String baseUrl,
            @Value("${code-atlas.github.token:}") String token,
            @Value("${code-atlas.github.owner:}") String owner,
            @Value("${code-atlas.github.repository:}") String repository
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.enabled = enabled;
        this.token = token;
        this.owner = owner;
        this.repository = repository;
    }

    public GitHubReleasePublishResponse publish(GitHubReleaseDraftResponse draft) {
        validateConfiguration();
        try {
            GitHubReleaseResponse response = restClient.post()
                    .uri("/repos/{owner}/{repository}/releases", owner, repository)
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .body(Map.of(
                            "tag_name", draft.tagName(),
                            "name", draft.releaseName(),
                            "body", draft.body(),
                            "draft", draft.draft(),
                            "prerelease", draft.prerelease()
                    ))
                    .retrieve()
                    .body(GitHubReleaseResponse.class);

            if (response == null) {
                throw new IllegalStateException("GitHub release publish returned an empty response.");
            }
            return response.toPublishResponse();
        } catch (RestClientException exception) {
            throw new IllegalStateException("GitHub release publish failed: " + exception.getMessage(), exception);
        }
    }

    private void validateConfiguration() {
        if (!enabled) {
            throw new IllegalArgumentException("GitHub publishing is disabled. Set CODE_ATLAS_GITHUB_ENABLED=true to enable it.");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("GitHub token is not configured. Set CODE_ATLAS_GITHUB_TOKEN.");
        }
        if (owner == null || owner.isBlank() || repository == null || repository.isBlank()) {
            throw new IllegalArgumentException("GitHub owner/repository is not configured. Set CODE_ATLAS_GITHUB_OWNER and CODE_ATLAS_GITHUB_REPOSITORY.");
        }
    }

    private record GitHubReleaseResponse(
            Long id,
            String tag_name,
            String name,
            String html_url,
            String url,
            boolean draft,
            boolean prerelease
    ) {

        private GitHubReleasePublishResponse toPublishResponse() {
            return new GitHubReleasePublishResponse(
                    id == null ? null : String.valueOf(id),
                    tag_name,
                    name,
                    html_url,
                    url,
                    draft,
                    prerelease
            );
        }
    }
}
