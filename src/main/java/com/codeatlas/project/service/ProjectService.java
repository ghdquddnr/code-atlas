package com.codeatlas.project.service;

import com.codeatlas.project.domain.Project;
import com.codeatlas.project.dto.CreateProjectRequest;
import com.codeatlas.project.dto.ProjectResponse;
import com.codeatlas.project.repository.ProjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        Project project = Project.create(request.name(), request.sourcePath());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long projectId) {
        return ProjectResponse.from(findProject(projectId));
    }

    @Transactional(readOnly = true)
    public Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
    }
}
