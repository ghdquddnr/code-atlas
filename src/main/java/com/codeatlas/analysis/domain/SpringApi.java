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
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "spring_apis")
public class SpringApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 20)
    private String httpMethod;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(nullable = false, length = 300)
    private String controllerClassName;

    @Column(nullable = false, length = 200)
    private String methodName;

    @Column(length = 200)
    private String requestDtoName;

    @Column(length = 200)
    private String responseDtoName;

    @Column(nullable = false, length = 1200)
    private String sourceFilePath;

    private Integer lineNumber;

    @Column(nullable = false)
    private Instant extractedAt;

    protected SpringApi() {
    }

    private SpringApi(
            Project project,
            String httpMethod,
            String path,
            String controllerClassName,
            String methodName,
            String requestDtoName,
            String responseDtoName,
            String sourceFilePath,
            Integer lineNumber
    ) {
        this.project = project;
        this.httpMethod = httpMethod;
        this.path = path;
        this.controllerClassName = controllerClassName;
        this.methodName = methodName;
        this.requestDtoName = requestDtoName;
        this.responseDtoName = responseDtoName;
        this.sourceFilePath = sourceFilePath;
        this.lineNumber = lineNumber;
        this.extractedAt = Instant.now();
    }

    public static SpringApi create(
            Project project,
            String httpMethod,
            String path,
            String controllerClassName,
            String methodName,
            String requestDtoName,
            String responseDtoName,
            String sourceFilePath,
            Integer lineNumber
    ) {
        return new SpringApi(project, httpMethod, path, controllerClassName, methodName, requestDtoName, responseDtoName, sourceFilePath, lineNumber);
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getRequestDtoName() {
        return requestDtoName;
    }

    public String getResponseDtoName() {
        return responseDtoName;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public Instant getExtractedAt() {
        return extractedAt;
    }
}
