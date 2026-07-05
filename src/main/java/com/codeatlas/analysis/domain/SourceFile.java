package com.codeatlas.analysis.domain;

import com.codeatlas.analyzer.java.JavaClassCategory;
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
@Table(name = "source_files")
public class SourceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 1200)
    private String path;

    @Column(nullable = false, length = 1200)
    private String relativePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SourceFileType type;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(length = 300)
    private String packageName;

    @Column(length = 200)
    private String className;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private JavaClassCategory classCategory;

    @Column(length = 3000)
    private String classFieldSummary;

    @Column(nullable = false)
    private Instant indexedAt;

    protected SourceFile() {
    }

    private SourceFile(
            Project project,
            String path,
            String relativePath,
            SourceFileType type,
            long sizeBytes,
            String packageName,
            String className,
            JavaClassCategory classCategory,
            String classFieldSummary
    ) {
        this.project = project;
        this.path = path;
        this.relativePath = relativePath;
        this.type = type;
        this.sizeBytes = sizeBytes;
        this.packageName = packageName;
        this.className = className;
        this.classCategory = classCategory;
        this.classFieldSummary = classFieldSummary;
        this.indexedAt = Instant.now();
    }

    public static SourceFile create(
            Project project,
            String path,
            String relativePath,
            SourceFileType type,
            long sizeBytes,
            String packageName,
            String className,
            JavaClassCategory classCategory,
            String classFieldSummary
    ) {
        return new SourceFile(project, path, relativePath, type, sizeBytes, packageName, className, classCategory, classFieldSummary);
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getPath() {
        return path;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public SourceFileType getType() {
        return type;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public JavaClassCategory getClassCategory() {
        return classCategory;
    }

    public String getClassFieldSummary() {
        return classFieldSummary;
    }

    public Instant getIndexedAt() {
        return indexedAt;
    }
}
