package com.codeatlas.analysis.service;

import com.codeatlas.analysis.domain.AnalysisJob;
import com.codeatlas.analysis.domain.AnalysisSnapshot;
import com.codeatlas.analysis.domain.SourceFile;
import com.codeatlas.analysis.dto.AnalysisComparisonResponse;
import com.codeatlas.analysis.dto.AnalysisDiffSectionResponse;
import com.codeatlas.analysis.dto.AnalysisSnapshotResponse;
import com.codeatlas.analysis.dto.GitHubReleaseDraftResponse;
import com.codeatlas.analysis.dto.GitHubReleasePublishResponse;
import com.codeatlas.analysis.dto.ReleaseRiskResponse;
import com.codeatlas.analysis.dto.ReleaseRiskTrendPointResponse;
import com.codeatlas.analysis.dto.UpdateAnalysisSnapshotMetadataRequest;
import com.codeatlas.document.dto.GeneratedDocumentResponse;
import com.codeatlas.analysis.repository.AnalysisSnapshotRepository;
import com.codeatlas.analysis.repository.AnalysisJobRepository;
import com.codeatlas.analysis.repository.ApiFlowRepository;
import com.codeatlas.analysis.repository.MyBatisStatementRepository;
import com.codeatlas.analysis.repository.SourceFileRepository;
import com.codeatlas.analysis.repository.SpringApiRepository;
import com.codeatlas.analyzer.java.JavaClassCategory;
import com.codeatlas.project.domain.Project;
import com.codeatlas.project.service.ProjectService;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisSnapshotService {

    private final ProjectService projectService;
    private final AnalysisJobRepository analysisJobRepository;
    private final AnalysisSnapshotRepository analysisSnapshotRepository;
    private final SpringApiRepository springApiRepository;
    private final MyBatisStatementRepository myBatisStatementRepository;
    private final SourceFileRepository sourceFileRepository;
    private final ApiFlowRepository apiFlowRepository;
    private final GitHubReleasePublisher gitHubReleasePublisher;

    public AnalysisSnapshotService(
            ProjectService projectService,
            AnalysisJobRepository analysisJobRepository,
            AnalysisSnapshotRepository analysisSnapshotRepository,
            SpringApiRepository springApiRepository,
            MyBatisStatementRepository myBatisStatementRepository,
            SourceFileRepository sourceFileRepository,
            ApiFlowRepository apiFlowRepository,
            GitHubReleasePublisher gitHubReleasePublisher
    ) {
        this.projectService = projectService;
        this.analysisJobRepository = analysisJobRepository;
        this.analysisSnapshotRepository = analysisSnapshotRepository;
        this.springApiRepository = springApiRepository;
        this.myBatisStatementRepository = myBatisStatementRepository;
        this.sourceFileRepository = sourceFileRepository;
        this.apiFlowRepository = apiFlowRepository;
        this.gitHubReleasePublisher = gitHubReleasePublisher;
    }

    @Transactional
    public AnalysisSnapshot capture(Project project, Long analysisJobId) {
        AnalysisJob analysisJob = analysisJobRepository.findById(analysisJobId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis job not found: " + analysisJobId));
        AnalysisSnapshot snapshot = AnalysisSnapshot.create(
                project,
                analysisJob,
                apiSummary(project.getId()),
                sqlSummary(project.getId()),
                dtoSummary(project.getId()),
                flowSummary(project.getId())
        );
        return analysisSnapshotRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public AnalysisComparisonResponse compareLatest(Long projectId) {
        projectService.findProject(projectId);
        List<AnalysisSnapshot> snapshots = analysisSnapshotRepository.findTop2ByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.size() < 2) {
            throw new IllegalStateException("At least two completed analysis snapshots are required for comparison.");
        }

        AnalysisSnapshot target = snapshots.get(0);
        AnalysisSnapshot base = snapshots.get(1);

        return compareSnapshots(projectId, base, target);
    }

    @Transactional(readOnly = true)
    public List<AnalysisSnapshotResponse> findSnapshots(Long projectId) {
        projectService.findProject(projectId);
        return analysisSnapshotRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(snapshot -> new AnalysisSnapshotResponse(
                        snapshot.getId(),
                        snapshot.getProject().getId(),
                        snapshot.getAnalysisJob().getId(),
                        snapshot.getCreatedAt(),
                        snapshot.getLabel(),
                        snapshot.getNote()
                ))
                .toList();
    }

    @Transactional
    public AnalysisSnapshotResponse updateMetadata(Long projectId, Long snapshotId, UpdateAnalysisSnapshotMetadataRequest request) {
        projectService.findProject(projectId);
        AnalysisSnapshot snapshot = findSnapshot(projectId, snapshotId);
        snapshot.updateMetadata(request.label(), request.note());
        return new AnalysisSnapshotResponse(
                snapshot.getId(),
                snapshot.getProject().getId(),
                snapshot.getAnalysisJob().getId(),
                snapshot.getCreatedAt(),
                snapshot.getLabel(),
                snapshot.getNote()
        );
    }

    @Transactional(readOnly = true)
    public AnalysisComparisonResponse compare(Long projectId, Long baseSnapshotId, Long targetSnapshotId) {
        projectService.findProject(projectId);
        if (baseSnapshotId.equals(targetSnapshotId)) {
            throw new IllegalArgumentException("Base and target snapshots must be different.");
        }
        AnalysisSnapshot base = findSnapshot(projectId, baseSnapshotId);
        AnalysisSnapshot target = findSnapshot(projectId, targetSnapshotId);
        return compareSnapshots(projectId, base, target);
    }

    @Transactional(readOnly = true)
    public List<ReleaseRiskTrendPointResponse> riskTrend(Long projectId) {
        projectService.findProject(projectId);
        List<AnalysisSnapshot> snapshots = analysisSnapshotRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .reversed();
        if (snapshots.size() < 2) {
            return List.of();
        }

        List<ReleaseRiskTrendPointResponse> points = new java.util.ArrayList<>();
        for (int index = 1; index < snapshots.size(); index++) {
            AnalysisSnapshot base = snapshots.get(index - 1);
            AnalysisSnapshot target = snapshots.get(index);
            AnalysisComparisonResponse comparison = compareSnapshots(projectId, base, target);
            points.add(new ReleaseRiskTrendPointResponse(
                    comparison.baseSnapshotId(),
                    comparison.targetSnapshotId(),
                    comparison.baseJobId(),
                    comparison.targetJobId(),
                    comparison.targetCreatedAt(),
                    comparison.releaseRisk(),
                    changeCount(comparison)
            ));
        }
        return List.copyOf(points);
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse generateLatestComparisonReport(Long projectId) {
        AnalysisComparisonResponse comparison = compareLatest(projectId);
        return toReport(comparison);
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse generateComparisonReport(Long projectId, Long baseSnapshotId, Long targetSnapshotId) {
        AnalysisComparisonResponse comparison = compare(projectId, baseSnapshotId, targetSnapshotId);
        return toReport(comparison);
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse generateLatestReleaseChecklist(Long projectId) {
        AnalysisComparisonResponse comparison = compareLatest(projectId);
        return toReleaseChecklist(comparison);
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse generateReleaseChecklist(Long projectId, Long baseSnapshotId, Long targetSnapshotId) {
        AnalysisComparisonResponse comparison = compare(projectId, baseSnapshotId, targetSnapshotId);
        return toReleaseChecklist(comparison);
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse generateLatestReleaseNotes(Long projectId) {
        projectService.findProject(projectId);
        List<AnalysisSnapshot> snapshots = analysisSnapshotRepository.findTop2ByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.size() < 2) {
            throw new IllegalStateException("At least two completed analysis snapshots are required for release notes.");
        }
        return toReleaseNotes(projectId, snapshots.get(1), snapshots.get(0));
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse generateReleaseNotes(Long projectId, Long baseSnapshotId, Long targetSnapshotId) {
        projectService.findProject(projectId);
        return toReleaseNotes(projectId, findSnapshot(projectId, baseSnapshotId), findSnapshot(projectId, targetSnapshotId));
    }

    @Transactional(readOnly = true)
    public GitHubReleaseDraftResponse generateLatestGitHubReleaseDraft(Long projectId) {
        projectService.findProject(projectId);
        List<AnalysisSnapshot> snapshots = analysisSnapshotRepository.findTop2ByProjectIdOrderByCreatedAtDesc(projectId);
        if (snapshots.size() < 2) {
            throw new IllegalStateException("At least two completed analysis snapshots are required for a release draft.");
        }
        return toGitHubReleaseDraft(projectId, snapshots.get(1), snapshots.get(0));
    }

    @Transactional(readOnly = true)
    public GitHubReleaseDraftResponse generateGitHubReleaseDraft(Long projectId, Long baseSnapshotId, Long targetSnapshotId) {
        projectService.findProject(projectId);
        return toGitHubReleaseDraft(projectId, findSnapshot(projectId, baseSnapshotId), findSnapshot(projectId, targetSnapshotId));
    }

    @Transactional(readOnly = true)
    public GitHubReleasePublishResponse publishLatestGitHubReleaseDraft(Long projectId) {
        return gitHubReleasePublisher.publish(generateLatestGitHubReleaseDraft(projectId));
    }

    @Transactional(readOnly = true)
    public GitHubReleasePublishResponse publishGitHubReleaseDraft(Long projectId, Long baseSnapshotId, Long targetSnapshotId) {
        return gitHubReleasePublisher.publish(generateGitHubReleaseDraft(projectId, baseSnapshotId, targetSnapshotId));
    }

    private AnalysisComparisonResponse compareSnapshots(
            Long projectId,
            AnalysisSnapshot base,
            AnalysisSnapshot target
    ) {
        AnalysisDiffSectionResponse apis = diff(base.getApiSummary(), target.getApiSummary());
        AnalysisDiffSectionResponse sqlStatements = diff(base.getSqlSummary(), target.getSqlSummary());
        AnalysisDiffSectionResponse dtos = diff(base.getDtoSummary(), target.getDtoSummary());
        AnalysisDiffSectionResponse flows = diff(base.getFlowSummary(), target.getFlowSummary());

        return new AnalysisComparisonResponse(
                projectId,
                base.getId(),
                target.getId(),
                base.getAnalysisJob().getId(),
                target.getAnalysisJob().getId(),
                base.getCreatedAt(),
                target.getCreatedAt(),
                apis,
                sqlStatements,
                dtos,
                flows,
                releaseRisk(apis, sqlStatements, dtos, flows)
        );
    }

    private AnalysisSnapshot findSnapshot(Long projectId, Long snapshotId) {
        return analysisSnapshotRepository.findByIdAndProjectId(snapshotId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis snapshot not found: " + snapshotId));
    }

    private GeneratedDocumentResponse toReport(AnalysisComparisonResponse comparison) {
        String content = """
                # Analysis Comparison Report

                ## Summary

                - Project ID: `%d`
                - Base snapshot: `%d` (job `%d`, %s)
                - Target snapshot: `%d` (job `%d`, %s)
                - Readiness score: `%d`
                - Risk level: `%s`
                - Risk reason: %s

                ## Change Counts

                | Area | Added | Removed |
                | --- | ---: | ---: |
                | APIs | %d | %d |
                | SQL Statements | %d | %d |
                | DTOs | %d | %d |
                | Flows | %d | %d |

                ## API Changes

                %s

                ## SQL Changes

                %s

                ## DTO Changes

                %s

                ## Flow Changes

                %s

                ## Review Guidance

                - Added items should be checked for new external behavior, new SQL/table dependency, and DTO contract changes.
                - Removed items should be checked for clients, batch jobs, tests, and documentation that still reference the old behavior.
                - If all sections show no changes, the two analysis snapshots are structurally equivalent by extracted CodeAtlas evidence.
                """.formatted(
                comparison.projectId(),
                comparison.baseSnapshotId(),
                comparison.baseJobId(),
                comparison.baseCreatedAt(),
                comparison.targetSnapshotId(),
                comparison.targetJobId(),
                comparison.targetCreatedAt(),
                comparison.releaseRisk().readinessScore(),
                comparison.releaseRisk().riskLevel(),
                comparison.releaseRisk().riskReason(),
                comparison.apis().addedCount(),
                comparison.apis().removedCount(),
                comparison.sqlStatements().addedCount(),
                comparison.sqlStatements().removedCount(),
                comparison.dtos().addedCount(),
                comparison.dtos().removedCount(),
                comparison.flows().addedCount(),
                comparison.flows().removedCount(),
                formatDiff(comparison.apis().added(), comparison.apis().removed()),
                formatDiff(comparison.sqlStatements().added(), comparison.sqlStatements().removed()),
                formatDiff(comparison.dtos().added(), comparison.dtos().removed()),
                formatDiff(comparison.flows().added(), comparison.flows().removed())
        );

        int evidenceCount = comparison.apis().addedCount() + comparison.apis().removedCount()
                + comparison.sqlStatements().addedCount() + comparison.sqlStatements().removedCount()
                + comparison.dtos().addedCount() + comparison.dtos().removedCount()
                + comparison.flows().addedCount() + comparison.flows().removedCount();

        return GeneratedDocumentResponse.markdown("Analysis Comparison Report", content, evidenceCount);
    }

    private String formatDiff(List<String> added, List<String> removed) {
        return """
                ### Added

                %s

                ### Removed

                %s
                """.formatted(formatItems(added), formatItems(removed));
    }

    private String formatItems(List<String> items) {
        if (items.isEmpty()) {
            return "No changes.";
        }
        return items.stream()
                .map(item -> "- `%s`".formatted(item))
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private GeneratedDocumentResponse toReleaseChecklist(AnalysisComparisonResponse comparison) {
        String content = """
                # Release Review Checklist

                ## Snapshot Scope

                - Project ID: `%d`
                - Base snapshot: `%d` (job `%d`, %s)
                - Target snapshot: `%d` (job `%d`, %s)
                - Readiness score: `%d`
                - Risk level: `%s`
                - Risk reason: %s

                ## Required Review

                %s

                ## API Review

                %s

                ## SQL and Table Review

                %s

                ## DTO Contract Review

                %s

                ## Flow and Integration Review

                %s

                ## Final Approval

                - [ ] All added API/SQL/DTO/Flow items have owner review.
                - [ ] Removed behavior has migration, deprecation, or client-impact confirmation.
                - [ ] Regression tests cover changed entry points and persistence paths.
                - [ ] Release notes include user-visible API or data contract changes.
                """.formatted(
                comparison.projectId(),
                comparison.baseSnapshotId(),
                comparison.baseJobId(),
                comparison.baseCreatedAt(),
                comparison.targetSnapshotId(),
                comparison.targetJobId(),
                comparison.targetCreatedAt(),
                comparison.releaseRisk().readinessScore(),
                comparison.releaseRisk().riskLevel(),
                comparison.releaseRisk().riskReason(),
                requiredReviewSummary(comparison),
                checklist("Added APIs", comparison.apis().added(), "Confirm request/response contract, auth, validation, and documentation."),
                checklist("Changed SQL evidence", merge(comparison.sqlStatements().added(), comparison.sqlStatements().removed()), "Confirm table impact, indexes, rollback, and data migration risk."),
                checklist("Changed DTOs", merge(comparison.dtos().added(), comparison.dtos().removed()), "Confirm field compatibility, client serialization, and null/default handling."),
                checklist("Changed Flows", merge(comparison.flows().added(), comparison.flows().removed()), "Confirm end-to-end integration test coverage and downstream table/mapper impact.")
        );

        return GeneratedDocumentResponse.markdown("Release Review Checklist", content, changeCount(comparison));
    }

    private GeneratedDocumentResponse toReleaseNotes(Long projectId, AnalysisSnapshot base, AnalysisSnapshot target) {
        AnalysisComparisonResponse comparison = compareSnapshots(projectId, base, target);
        String releaseName = target.getLabel() == null ? "Snapshot " + target.getId() : target.getLabel();
        String previousName = base.getLabel() == null ? "Snapshot " + base.getId() : base.getLabel();

        String content = """
                # Release Notes - %s

                ## Release Scope

                - Previous baseline: `%s` (snapshot `%d`, job `%d`)
                - Target release: `%s` (snapshot `%d`, job `%d`)
                - Target note: %s
                - Readiness score: `%d`
                - Risk level: `%s`

                ## Summary

                %s

                ## Added or Changed APIs

                %s

                ## SQL and Data Access Changes

                %s

                ## DTO Contract Changes

                %s

                ## Flow Changes

                %s

                ## Compatibility Notes

                - Review API and DTO entries for client-facing contract changes.
                - Review SQL and Flow entries for database and integration impact.
                - This document is generated from extracted CodeAtlas analysis evidence.
                """.formatted(
                releaseName,
                previousName,
                base.getId(),
                base.getAnalysisJob().getId(),
                releaseName,
                target.getId(),
                target.getAnalysisJob().getId(),
                nullToDash(target.getNote()),
                comparison.releaseRisk().readinessScore(),
                comparison.releaseRisk().riskLevel(),
                releaseSummary(comparison),
                releaseItems(comparison.apis().added(), comparison.apis().removed()),
                releaseItems(comparison.sqlStatements().added(), comparison.sqlStatements().removed()),
                releaseItems(comparison.dtos().added(), comparison.dtos().removed()),
                releaseItems(comparison.flows().added(), comparison.flows().removed())
        );

        return GeneratedDocumentResponse.markdown("Release Notes - " + releaseName, content, changeCount(comparison));
    }

    private GitHubReleaseDraftResponse toGitHubReleaseDraft(Long projectId, AnalysisSnapshot base, AnalysisSnapshot target) {
        GeneratedDocumentResponse releaseNotes = toReleaseNotes(projectId, base, target);
        String releaseName = target.getLabel() == null ? "Snapshot " + target.getId() : target.getLabel();
        String tagName = toTagName(releaseName, target.getId());
        return new GitHubReleaseDraftResponse(
                tagName,
                releaseName,
                releaseNotes.content(),
                true,
                isPrerelease(releaseName)
        );
    }

    private String toTagName(String releaseName, Long snapshotId) {
        String normalized = releaseName.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("^-+|-+$", "");
        if (normalized.isBlank()) {
            return "snapshot-" + snapshotId;
        }
        return normalized.startsWith("v") ? normalized : "v-" + normalized;
    }

    private boolean isPrerelease(String releaseName) {
        String normalized = releaseName.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("rc")
                || normalized.contains("alpha")
                || normalized.contains("beta")
                || normalized.contains("snapshot");
    }

    private String releaseSummary(AnalysisComparisonResponse comparison) {
        int totalChanges = changeCount(comparison);
        if (totalChanges == 0) {
            return "No extracted API, SQL, DTO, or Flow changes were detected between the selected snapshots.";
        }
        return "%d extracted structural change(s) were detected. Risk level is `%s` with readiness score `%d`.".formatted(
                totalChanges,
                comparison.releaseRisk().riskLevel(),
                comparison.releaseRisk().readinessScore()
        );
    }

    private String releaseItems(List<String> added, List<String> removed) {
        if (added.isEmpty() && removed.isEmpty()) {
            return "No extracted changes.";
        }
        String addedItems = added.stream()
                .map(item -> "- Added: `%s`".formatted(item))
                .collect(java.util.stream.Collectors.joining("\n"));
        String removedItems = removed.stream()
                .map(item -> "- Removed: `%s`".formatted(item))
                .collect(java.util.stream.Collectors.joining("\n"));
        return java.util.stream.Stream.of(addedItems, removedItems)
                .filter(section -> !section.isBlank())
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private String requiredReviewSummary(AnalysisComparisonResponse comparison) {
        int totalChanges = changeCount(comparison);
        if (totalChanges == 0) {
            return "- [ ] No extracted structural changes. Confirm build, tests, and deployment metadata only.";
        }
        return "- [ ] Review `%d` extracted structural change(s) before release approval.".formatted(totalChanges);
    }

    private String checklist(String title, List<String> items, String reviewAction) {
        if (items.isEmpty()) {
            return "- [ ] %s: no extracted changes.".formatted(title);
        }
        return items.stream()
                .map(item -> "- [ ] `%s` - %s".formatted(item, reviewAction))
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private List<String> merge(List<String> left, List<String> right) {
        return java.util.stream.Stream.concat(left.stream(), right.stream())
                .sorted()
                .toList();
    }

    private int changeCount(AnalysisComparisonResponse comparison) {
        return comparison.apis().addedCount() + comparison.apis().removedCount()
                + comparison.sqlStatements().addedCount() + comparison.sqlStatements().removedCount()
                + comparison.dtos().addedCount() + comparison.dtos().removedCount()
                + comparison.flows().addedCount() + comparison.flows().removedCount();
    }

    private ReleaseRiskResponse releaseRisk(
            AnalysisDiffSectionResponse apis,
            AnalysisDiffSectionResponse sqlStatements,
            AnalysisDiffSectionResponse dtos,
            AnalysisDiffSectionResponse flows
    ) {
        int apiChanges = apis.addedCount() + apis.removedCount();
        int sqlChanges = sqlStatements.addedCount() + sqlStatements.removedCount();
        int dtoChanges = dtos.addedCount() + dtos.removedCount();
        int flowChanges = flows.addedCount() + flows.removedCount();
        int weightedRisk = apiChanges * 8 + sqlChanges * 7 + dtoChanges * 6 + flowChanges * 5;
        int readinessScore = Math.max(0, 100 - weightedRisk);

        if (weightedRisk >= 45 || apiChanges >= 5 || sqlChanges >= 5) {
            return new ReleaseRiskResponse(readinessScore, "HIGH", "High structural change volume or broad API/SQL changes require release approval.");
        }
        if (weightedRisk >= 18 || apiChanges >= 2 || dtoChanges >= 3 || flowChanges >= 4) {
            return new ReleaseRiskResponse(readinessScore, "MEDIUM", "Multiple extracted changes require targeted owner review.");
        }
        if (weightedRisk > 0) {
            return new ReleaseRiskResponse(readinessScore, "LOW", "Limited extracted changes were detected.");
        }
        return new ReleaseRiskResponse(100, "NONE", "No extracted structural changes were detected.");
    }

    private String apiSummary(Long projectId) {
        return springApiRepository.findByProjectIdOrderByPathAscHttpMethodAsc(projectId).stream()
                .map(api -> api.getHttpMethod() + " " + api.getPath()
                        + " -> " + api.getControllerClassName() + "." + api.getMethodName()
                        + " request=" + nullToDash(api.getRequestDtoName())
                        + " response=" + nullToDash(api.getResponseDtoName()))
                .sorted()
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private String sqlSummary(Long projectId) {
        return myBatisStatementRepository.findByProjectIdOrderByNamespaceAscStatementIdAsc(projectId).stream()
                .map(statement -> statement.getNamespace() + "." + statement.getStatementId()
                        + " [" + statement.getStatementType() + "]"
                        + " tables=" + String.join(",", statement.getTableNames()))
                .sorted()
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private String dtoSummary(Long projectId) {
        return sourceFileRepository.findByProjectIdOrderByRelativePath(projectId).stream()
                .filter(sourceFile -> sourceFile.getClassCategory() == JavaClassCategory.DTO)
                .map(this::dtoKey)
                .sorted()
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private String dtoKey(SourceFile sourceFile) {
        return sourceFile.getClassName() + " fields=" + nullToDash(sourceFile.getClassFieldSummary());
    }

    private String flowSummary(Long projectId) {
        return apiFlowRepository.findByProjectIdOrderByApiPathAscHttpMethodAscMapperStatementIdAsc(projectId).stream()
                .map(flow -> flow.getHttpMethod() + " " + flow.getApiPath()
                        + " -> " + flow.getServiceClassName() + "." + flow.getServiceMethodName()
                        + " -> " + flow.getMapperNamespace() + "." + flow.getMapperStatementId()
                        + " tables=" + String.join(",", flow.getTableNames()))
                .sorted()
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private AnalysisDiffSectionResponse diff(String baseSummary, String targetSummary) {
        Set<String> base = lines(baseSummary);
        Set<String> target = lines(targetSummary);

        List<String> added = target.stream()
                .filter(item -> !base.contains(item))
                .toList();
        List<String> removed = base.stream()
                .filter(item -> !target.contains(item))
                .toList();

        return new AnalysisDiffSectionResponse(added.size(), removed.size(), added, removed);
    }

    private Set<String> lines(String summary) {
        if (summary == null || summary.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(summary.split("\\n"))
                .filter(line -> !line.isBlank())
                .collect(java.util.stream.Collectors.toCollection(TreeSet::new));
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
