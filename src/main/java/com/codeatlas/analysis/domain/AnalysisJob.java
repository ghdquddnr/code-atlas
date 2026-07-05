package com.codeatlas.analysis.domain;

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
@Table(name = "analysis_jobs")
public class AnalysisJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnalysisJobStatus status;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant startedAt;

    private Instant finishedAt;

    protected AnalysisJob() {
    }

    private AnalysisJob(Project project) {
        this.project = project;
        this.status = AnalysisJobStatus.PENDING;
        this.message = "Analysis job has been created.";
        this.createdAt = Instant.now();
    }

    public static AnalysisJob create(Project project) {
        return new AnalysisJob(project);
    }

    public void markRunning() {
        this.status = AnalysisJobStatus.RUNNING;
        this.message = "Analysis job is running.";
        this.startedAt = Instant.now();
    }

    public void markCompleted(String message) {
        this.status = AnalysisJobStatus.COMPLETED;
        this.message = message;
        this.startedAt = this.startedAt == null ? Instant.now() : this.startedAt;
        this.finishedAt = Instant.now();
    }

    public void markFailed(String message) {
        this.status = AnalysisJobStatus.FAILED;
        this.message = message;
        this.startedAt = this.startedAt == null ? Instant.now() : this.startedAt;
        this.finishedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public AnalysisJobStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }
}
