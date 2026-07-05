package com.codeatlas.analysis.domain;

import com.codeatlas.project.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "analysis_snapshots")
public class AnalysisSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_job_id", nullable = false, unique = true)
    private AnalysisJob analysisJob;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(length = 200)
    private String label;

    @Column(length = 1000)
    private String note;

    @Column(length = 10000)
    private String apiSummary;

    @Column(length = 10000)
    private String sqlSummary;

    @Column(length = 10000)
    private String dtoSummary;

    @Column(length = 10000)
    private String flowSummary;

    protected AnalysisSnapshot() {
    }

    private AnalysisSnapshot(
            Project project,
            AnalysisJob analysisJob,
            String apiSummary,
            String sqlSummary,
            String dtoSummary,
            String flowSummary
    ) {
        this.project = project;
        this.analysisJob = analysisJob;
        this.createdAt = Instant.now();
        this.apiSummary = apiSummary;
        this.sqlSummary = sqlSummary;
        this.dtoSummary = dtoSummary;
        this.flowSummary = flowSummary;
    }

    public static AnalysisSnapshot create(
            Project project,
            AnalysisJob analysisJob,
            String apiSummary,
            String sqlSummary,
            String dtoSummary,
            String flowSummary
    ) {
        return new AnalysisSnapshot(project, analysisJob, apiSummary, sqlSummary, dtoSummary, flowSummary);
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public AnalysisJob getAnalysisJob() {
        return analysisJob;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getApiSummary() {
        return apiSummary;
    }

    public String getSqlSummary() {
        return sqlSummary;
    }

    public String getDtoSummary() {
        return dtoSummary;
    }

    public String getFlowSummary() {
        return flowSummary;
    }

    public String getLabel() {
        return label;
    }

    public String getNote() {
        return note;
    }

    public void updateMetadata(String label, String note) {
        this.label = normalize(label, 200);
        this.note = normalize(note, 1000);
    }

    private String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
