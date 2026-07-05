package com.codeatlas.document.controller;

import com.codeatlas.common.response.ApiResponse;
import com.codeatlas.document.dto.GeneratedDocumentResponse;
import com.codeatlas.document.service.DocumentationService;
import com.codeatlas.project.service.ProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/documents")
public class DocumentationController {

    private final DocumentationService documentationService;
    private final ProjectService projectService;

    public DocumentationController(DocumentationService documentationService, ProjectService projectService) {
        this.documentationService = documentationService;
        this.projectService = projectService;
    }

    @GetMapping("/onboarding")
    public ApiResponse<GeneratedDocumentResponse> generateOnboardingDocument(@PathVariable Long projectId) {
        projectService.findProject(projectId);
        return ApiResponse.ok(documentationService.generateOnboardingDocument(projectId));
    }

    @GetMapping("/apis")
    public ApiResponse<GeneratedDocumentResponse> generateApiDocument(@PathVariable Long projectId) {
        projectService.findProject(projectId);
        return ApiResponse.ok(documentationService.generateApiDocument(projectId));
    }
}
