package com.codeatlas.project.controller;

import com.codeatlas.common.response.ApiResponse;
import com.codeatlas.project.dto.CreateProjectRequest;
import com.codeatlas.project.dto.ProjectDeleteResponse;
import com.codeatlas.project.dto.ProjectResponse;
import com.codeatlas.project.service.ProjectDeletionService;
import com.codeatlas.project.service.ProjectService;
import com.codeatlas.project.service.ProjectUploadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectUploadService projectUploadService;
    private final ProjectDeletionService projectDeletionService;

    public ProjectController(
            ProjectService projectService,
            ProjectUploadService projectUploadService,
            ProjectDeletionService projectDeletionService
    ) {
        this.projectService = projectService;
        this.projectUploadService = projectUploadService;
        this.projectDeletionService = projectDeletionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        return ApiResponse.ok(projectService.create(request));
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectResponse> upload(
            @RequestParam String name,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.ok(projectUploadService.upload(name, file));
    }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> findAll() {
        return ApiResponse.ok(projectService.findAll());
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponse> getById(@PathVariable Long projectId) {
        return ApiResponse.ok(projectService.getById(projectId));
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<ProjectDeleteResponse> delete(@PathVariable Long projectId) {
        return ApiResponse.ok(projectDeletionService.delete(projectId));
    }
}
