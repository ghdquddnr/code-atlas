package com.codeatlas.analysis.domain;

import com.codeatlas.project.domain.Project;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "api_flows")
public class ApiFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 20)
    private String httpMethod;

    @Column(nullable = false, length = 500)
    private String apiPath;

    @Column(nullable = false, length = 300)
    private String controllerClassName;

    @Column(nullable = false, length = 200)
    private String controllerMethodName;

    @Column(length = 300)
    private String serviceClassName;

    @Column(length = 200)
    private String serviceMethodName;

    @Column(length = 500)
    private String mapperNamespace;

    @Column(length = 200)
    private String mapperStatementId;

    @Column(length = 20)
    private String mapperStatementType;

    @Column(length = 3000)
    private String methodCallPathSummary;

    @ElementCollection
    @CollectionTable(name = "api_flow_tables", joinColumns = @JoinColumn(name = "api_flow_id"))
    @Column(name = "table_name", nullable = false, length = 200)
    private Set<String> tableNames = new LinkedHashSet<>();

    @Column(nullable = false)
    private Instant mappedAt;

    protected ApiFlow() {
    }

    private ApiFlow(
            Project project,
            String httpMethod,
            String apiPath,
            String controllerClassName,
            String controllerMethodName,
            String serviceClassName,
            String serviceMethodName,
            String mapperNamespace,
            String mapperStatementId,
            String mapperStatementType,
            String methodCallPathSummary,
            Set<String> tableNames
    ) {
        this.project = project;
        this.httpMethod = httpMethod;
        this.apiPath = apiPath;
        this.controllerClassName = controllerClassName;
        this.controllerMethodName = controllerMethodName;
        this.serviceClassName = serviceClassName;
        this.serviceMethodName = serviceMethodName;
        this.mapperNamespace = mapperNamespace;
        this.mapperStatementId = mapperStatementId;
        this.mapperStatementType = mapperStatementType;
        this.methodCallPathSummary = methodCallPathSummary;
        this.tableNames = new LinkedHashSet<>(tableNames);
        this.mappedAt = Instant.now();
    }

    public static ApiFlow create(
            Project project,
            String httpMethod,
            String apiPath,
            String controllerClassName,
            String controllerMethodName,
            String serviceClassName,
            String serviceMethodName,
            String mapperNamespace,
            String mapperStatementId,
            String mapperStatementType,
            String methodCallPathSummary,
            Set<String> tableNames
    ) {
        return new ApiFlow(
                project,
                httpMethod,
                apiPath,
                controllerClassName,
                controllerMethodName,
                serviceClassName,
                serviceMethodName,
                mapperNamespace,
                mapperStatementId,
                mapperStatementType,
                methodCallPathSummary,
                tableNames
        );
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

    public String getApiPath() {
        return apiPath;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    public String getControllerMethodName() {
        return controllerMethodName;
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public String getServiceMethodName() {
        return serviceMethodName;
    }

    public String getMapperNamespace() {
        return mapperNamespace;
    }

    public String getMapperStatementId() {
        return mapperStatementId;
    }

    public String getMapperStatementType() {
        return mapperStatementType;
    }

    public String getMethodCallPathSummary() {
        return methodCallPathSummary;
    }

    public Set<String> getTableNames() {
        return tableNames;
    }

    public Instant getMappedAt() {
        return mappedAt;
    }
}
