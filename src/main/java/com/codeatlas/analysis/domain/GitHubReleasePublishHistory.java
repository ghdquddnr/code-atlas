package com.codeatlas.analysis.domain;

import com.codeatlas.analysis.dto.GitHubReleaseDraftResponse;
import com.codeatlas.analysis.dto.GitHubReleasePublishResponse;
import com.codeatlas.project.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "github_release_publish_history")
public class GitHubReleasePublishHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_snapshot_id", nullable = false)
    private AnalysisSnapshot baseSnapshot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_snapshot_id", nullable = false)
    private AnalysisSnapshot targetSnapshot;

    @Column(nullable = false)
    private Instant requestedAt;

    @Column
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GitHubReleasePublishStatus status;

    @Column(nullable = false, length = 200)
    private String tagName;

    @Column(nullable = false, length = 300)
    private String releaseName;

    @Column(length = 100)
    private String releaseId;

    @Column(length = 1000)
    private String htmlUrl;

    @Column(length = 1000)
    private String apiUrl;

    @Column(nullable = false)
    private boolean draft;

    @Column(nullable = false)
    private boolean prerelease;

    @Column(length = 2000)
    private String errorMessage;

    protected GitHubReleasePublishHistory() {
    }

    private GitHubReleasePublishHistory(
            Project project,
            AnalysisSnapshot baseSnapshot,
            AnalysisSnapshot targetSnapshot,
            GitHubReleaseDraftResponse draft
    ) {
        this.project = project;
        this.baseSnapshot = baseSnapshot;
        this.targetSnapshot = targetSnapshot;
        this.requestedAt = Instant.now();
        this.status = GitHubReleasePublishStatus.PENDING;
        this.tagName = draft.tagName();
        this.releaseName = draft.releaseName();
        this.draft = draft.draft();
        this.prerelease = draft.prerelease();
    }

    public static GitHubReleasePublishHistory pending(
            Project project,
            AnalysisSnapshot baseSnapshot,
            AnalysisSnapshot targetSnapshot,
            GitHubReleaseDraftResponse draft
    ) {
        return new GitHubReleasePublishHistory(project, baseSnapshot, targetSnapshot, draft);
    }

    public void markSucceeded(GitHubReleasePublishResponse response) {
        this.completedAt = Instant.now();
        this.status = GitHubReleasePublishStatus.SUCCESS;
        this.releaseId = response.id();
        this.tagName = response.tagName();
        this.releaseName = response.releaseName();
        this.htmlUrl = response.htmlUrl();
        this.apiUrl = response.apiUrl();
        this.draft = response.draft();
        this.prerelease = response.prerelease();
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.completedAt = Instant.now();
        this.status = GitHubReleasePublishStatus.FAILED;
        this.errorMessage = normalize(errorMessage, 2000);
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public AnalysisSnapshot getBaseSnapshot() {
        return baseSnapshot;
    }

    public AnalysisSnapshot getTargetSnapshot() {
        return targetSnapshot;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public GitHubReleasePublishStatus getStatus() {
        return status;
    }

    public String getTagName() {
        return tagName;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public boolean isDraft() {
        return draft;
    }

    public boolean isPrerelease() {
        return prerelease;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
