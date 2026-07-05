package com.codeatlas.analysis.controller;

import com.codeatlas.analysis.dto.AnalysisComparisonResponse;
import com.codeatlas.analysis.dto.AnalysisSnapshotResponse;
import com.codeatlas.analysis.dto.GitHubReleaseDraftResponse;
import com.codeatlas.analysis.dto.GitHubReleasePublishHistoryResponse;
import com.codeatlas.analysis.dto.ReleaseRiskTrendPointResponse;
import com.codeatlas.analysis.dto.UpdateAnalysisSnapshotMetadataRequest;
import com.codeatlas.analysis.service.AnalysisSnapshotService;
import com.codeatlas.common.response.ApiResponse;
import com.codeatlas.document.dto.GeneratedDocumentResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/analysis")
public class AnalysisComparisonController {

    private final AnalysisSnapshotService analysisSnapshotService;

    public AnalysisComparisonController(AnalysisSnapshotService analysisSnapshotService) {
        this.analysisSnapshotService = analysisSnapshotService;
    }

    @GetMapping("/snapshots")
    public ApiResponse<List<AnalysisSnapshotResponse>> findSnapshots(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisSnapshotService.findSnapshots(projectId));
    }

    @PatchMapping("/snapshots/{snapshotId}")
    public ApiResponse<AnalysisSnapshotResponse> updateSnapshotMetadata(
            @PathVariable Long projectId,
            @PathVariable Long snapshotId,
            @RequestBody UpdateAnalysisSnapshotMetadataRequest request
    ) {
        return ApiResponse.ok(analysisSnapshotService.updateMetadata(projectId, snapshotId, request));
    }

    @GetMapping("/risk-trend")
    public ApiResponse<List<ReleaseRiskTrendPointResponse>> riskTrend(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisSnapshotService.riskTrend(projectId));
    }

    @GetMapping("/github-release-publish-history")
    public ApiResponse<List<GitHubReleasePublishHistoryResponse>> findGitHubReleasePublishHistory(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisSnapshotService.findGitHubReleasePublishHistory(projectId));
    }

    @GetMapping("/comparison/latest")
    public ApiResponse<AnalysisComparisonResponse> compareLatest(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisSnapshotService.compareLatest(projectId));
    }

    @GetMapping("/comparison/latest/report")
    public ApiResponse<GeneratedDocumentResponse> generateLatestComparisonReport(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisSnapshotService.generateLatestComparisonReport(projectId));
    }

    @GetMapping("/comparison/latest/report/download")
    public ResponseEntity<byte[]> downloadLatestComparisonReport(@PathVariable Long projectId) {
        GeneratedDocumentResponse report = analysisSnapshotService.generateLatestComparisonReport(projectId);
        return markdownDownload(report, "code-atlas-comparison-latest.md");
    }

    @GetMapping("/comparison/latest/checklist")
    public ApiResponse<GeneratedDocumentResponse> generateLatestReleaseChecklist(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisSnapshotService.generateLatestReleaseChecklist(projectId));
    }

    @GetMapping("/comparison/latest/checklist/download")
    public ResponseEntity<byte[]> downloadLatestReleaseChecklist(@PathVariable Long projectId) {
        GeneratedDocumentResponse checklist = analysisSnapshotService.generateLatestReleaseChecklist(projectId);
        return markdownDownload(checklist, "code-atlas-release-checklist-latest.md");
    }

    @GetMapping("/comparison/latest/release-notes")
    public ApiResponse<GeneratedDocumentResponse> generateLatestReleaseNotes(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisSnapshotService.generateLatestReleaseNotes(projectId));
    }

    @GetMapping("/comparison/latest/release-notes/download")
    public ResponseEntity<byte[]> downloadLatestReleaseNotes(@PathVariable Long projectId) {
        GeneratedDocumentResponse releaseNotes = analysisSnapshotService.generateLatestReleaseNotes(projectId);
        return markdownDownload(releaseNotes, "code-atlas-release-notes-latest.md");
    }

    @GetMapping("/comparison/latest/github-release-draft")
    public ApiResponse<GitHubReleaseDraftResponse> generateLatestGitHubReleaseDraft(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisSnapshotService.generateLatestGitHubReleaseDraft(projectId));
    }

    @PostMapping("/comparison/latest/github-release-draft/publish")
    public ApiResponse<GitHubReleasePublishHistoryResponse> publishLatestGitHubReleaseDraft(@PathVariable Long projectId) {
        return ApiResponse.ok(analysisSnapshotService.publishLatestGitHubReleaseDraft(projectId));
    }

    @GetMapping("/comparison")
    public ApiResponse<AnalysisComparisonResponse> compare(
            @PathVariable Long projectId,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId
    ) {
        return ApiResponse.ok(analysisSnapshotService.compare(projectId, baseSnapshotId, targetSnapshotId));
    }

    @GetMapping("/comparison/report")
    public ApiResponse<GeneratedDocumentResponse> generateComparisonReport(
            @PathVariable Long projectId,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId
    ) {
        return ApiResponse.ok(analysisSnapshotService.generateComparisonReport(projectId, baseSnapshotId, targetSnapshotId));
    }

    @GetMapping("/comparison/report/download")
    public ResponseEntity<byte[]> downloadComparisonReport(
            @PathVariable Long projectId,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId
    ) {
        GeneratedDocumentResponse report = analysisSnapshotService.generateComparisonReport(projectId, baseSnapshotId, targetSnapshotId);
        return markdownDownload(report, "code-atlas-comparison-%d-to-%d.md".formatted(baseSnapshotId, targetSnapshotId));
    }

    @GetMapping("/comparison/checklist")
    public ApiResponse<GeneratedDocumentResponse> generateReleaseChecklist(
            @PathVariable Long projectId,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId
    ) {
        return ApiResponse.ok(analysisSnapshotService.generateReleaseChecklist(projectId, baseSnapshotId, targetSnapshotId));
    }

    @GetMapping("/comparison/checklist/download")
    public ResponseEntity<byte[]> downloadReleaseChecklist(
            @PathVariable Long projectId,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId
    ) {
        GeneratedDocumentResponse checklist = analysisSnapshotService.generateReleaseChecklist(projectId, baseSnapshotId, targetSnapshotId);
        return markdownDownload(checklist, "code-atlas-release-checklist-%d-to-%d.md".formatted(baseSnapshotId, targetSnapshotId));
    }

    @GetMapping("/comparison/release-notes")
    public ApiResponse<GeneratedDocumentResponse> generateReleaseNotes(
            @PathVariable Long projectId,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId
    ) {
        return ApiResponse.ok(analysisSnapshotService.generateReleaseNotes(projectId, baseSnapshotId, targetSnapshotId));
    }

    @GetMapping("/comparison/release-notes/download")
    public ResponseEntity<byte[]> downloadReleaseNotes(
            @PathVariable Long projectId,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId
    ) {
        GeneratedDocumentResponse releaseNotes = analysisSnapshotService.generateReleaseNotes(projectId, baseSnapshotId, targetSnapshotId);
        return markdownDownload(releaseNotes, "code-atlas-release-notes-%d-to-%d.md".formatted(baseSnapshotId, targetSnapshotId));
    }

    @GetMapping("/comparison/github-release-draft")
    public ApiResponse<GitHubReleaseDraftResponse> generateGitHubReleaseDraft(
            @PathVariable Long projectId,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId
    ) {
        return ApiResponse.ok(analysisSnapshotService.generateGitHubReleaseDraft(projectId, baseSnapshotId, targetSnapshotId));
    }

    @PostMapping("/comparison/github-release-draft/publish")
    public ApiResponse<GitHubReleasePublishHistoryResponse> publishGitHubReleaseDraft(
            @PathVariable Long projectId,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId
    ) {
        return ApiResponse.ok(analysisSnapshotService.publishGitHubReleaseDraft(projectId, baseSnapshotId, targetSnapshotId));
    }

    private ResponseEntity<byte[]> markdownDownload(GeneratedDocumentResponse report, String filename) {
        byte[] content = report.content().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(content);
    }
}
