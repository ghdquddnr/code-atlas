package com.codeatlas.analysis.domain;

import com.codeatlas.project.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "mybatis_statements")
public class MyBatisStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 500)
    private String namespace;

    @Column(nullable = false, length = 200)
    private String statementId;

    @Column(nullable = false, length = 20)
    private String statementType;

    @Column(nullable = false, columnDefinition = "text")
    private String sql;

    @ElementCollection
    @CollectionTable(name = "mybatis_statement_tables", joinColumns = @JoinColumn(name = "statement_id"))
    @Column(name = "table_name", nullable = false, length = 200)
    private Set<String> tableNames = new LinkedHashSet<>();

    @Column(length = 500)
    private String parameterType;

    @Column(length = 500)
    private String resultType;

    @Column(nullable = false, length = 1200)
    private String sourceFilePath;

    @Column(nullable = false)
    private Instant extractedAt;

    protected MyBatisStatement() {
    }

    private MyBatisStatement(
            Project project,
            String namespace,
            String statementId,
            String statementType,
            String sql,
            Set<String> tableNames,
            String parameterType,
            String resultType,
            String sourceFilePath
    ) {
        this.project = project;
        this.namespace = namespace;
        this.statementId = statementId;
        this.statementType = statementType;
        this.sql = sql;
        this.tableNames = new LinkedHashSet<>(tableNames);
        this.parameterType = parameterType;
        this.resultType = resultType;
        this.sourceFilePath = sourceFilePath;
        this.extractedAt = Instant.now();
    }

    public static MyBatisStatement create(
            Project project,
            String namespace,
            String statementId,
            String statementType,
            String sql,
            Set<String> tableNames,
            String parameterType,
            String resultType,
            String sourceFilePath
    ) {
        return new MyBatisStatement(
                project,
                namespace,
                statementId,
                statementType,
                sql,
                tableNames,
                parameterType,
                resultType,
                sourceFilePath
        );
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getStatementId() {
        return statementId;
    }

    public String getStatementType() {
        return statementType;
    }

    public String getSql() {
        return sql;
    }

    public Set<String> getTableNames() {
        return tableNames;
    }

    public String getParameterType() {
        return parameterType;
    }

    public String getResultType() {
        return resultType;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public Instant getExtractedAt() {
        return extractedAt;
    }
}
