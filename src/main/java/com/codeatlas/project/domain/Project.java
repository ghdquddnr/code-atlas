package com.codeatlas.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 1000)
    private String sourcePath;

    @Column(nullable = false)
    private Instant createdAt;

    protected Project() {
    }

    private Project(String name, String sourcePath) {
        this.name = name;
        this.sourcePath = sourcePath;
        this.createdAt = Instant.now();
    }

    public static Project create(String name, String sourcePath) {
        return new Project(name, sourcePath);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
