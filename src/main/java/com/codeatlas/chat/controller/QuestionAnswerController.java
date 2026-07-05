package com.codeatlas.chat.controller;

import com.codeatlas.chat.dto.AskQuestionRequest;
import com.codeatlas.chat.dto.QuestionAnswerResponse;
import com.codeatlas.chat.service.QuestionAnswerService;
import com.codeatlas.common.response.ApiResponse;
import com.codeatlas.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/questions")
public class QuestionAnswerController {

    private final QuestionAnswerService questionAnswerService;
    private final ProjectService projectService;

    public QuestionAnswerController(QuestionAnswerService questionAnswerService, ProjectService projectService) {
        this.questionAnswerService = questionAnswerService;
        this.projectService = projectService;
    }

    @PostMapping
    public ApiResponse<QuestionAnswerResponse> answer(
            @PathVariable Long projectId,
            @Valid @RequestBody AskQuestionRequest request
    ) {
        projectService.findProject(projectId);
        return ApiResponse.ok(questionAnswerService.answer(projectId, request.question()));
    }
}
