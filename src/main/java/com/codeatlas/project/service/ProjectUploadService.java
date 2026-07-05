package com.codeatlas.project.service;

import com.codeatlas.project.domain.Project;
import com.codeatlas.project.dto.ProjectResponse;
import com.codeatlas.project.repository.ProjectRepository;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProjectUploadService {

    private final ProjectRepository projectRepository;
    private final ZipProjectExtractor zipProjectExtractor;
    private final Path projectsStoragePath;

    public ProjectUploadService(
            ProjectRepository projectRepository,
            ZipProjectExtractor zipProjectExtractor,
            @Value("${code-atlas.storage.projects-path:data/projects}") String projectsStoragePath
    ) {
        this.projectRepository = projectRepository;
        this.zipProjectExtractor = zipProjectExtractor;
        this.projectsStoragePath = Path.of(projectsStoragePath).toAbsolutePath().normalize();
    }

    @Transactional
    public ProjectResponse upload(String name, MultipartFile zipFile) {
        validate(name, zipFile);

        Path targetDirectory = projectsStoragePath
                .resolve(slug(name) + "-" + UUID.randomUUID())
                .normalize();
        zipProjectExtractor.extract(zipFile, targetDirectory);

        Project project = Project.create(name, targetDirectory.toString());
        return ProjectResponse.from(projectRepository.save(project));
    }

    private void validate(String name, MultipartFile zipFile) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name is required.");
        }
        if (zipFile == null || zipFile.isEmpty()) {
            throw new IllegalArgumentException("ZIP file is required.");
        }
        String fileName = zipFile.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Only .zip files are supported.");
        }
    }

    private String slug(String name) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9가-힣_-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "project" : slug;
    }
}
