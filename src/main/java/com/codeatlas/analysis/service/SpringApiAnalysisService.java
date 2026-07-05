package com.codeatlas.analysis.service;

import com.codeatlas.analysis.domain.SourceFileType;
import com.codeatlas.analysis.domain.SpringApi;
import com.codeatlas.analysis.dto.ApiFlowResponse;
import com.codeatlas.analysis.dto.SpringApiDetailResponse;
import com.codeatlas.analysis.dto.SpringApiResponse;
import com.codeatlas.analysis.repository.ApiFlowRepository;
import com.codeatlas.analysis.repository.SourceFileRepository;
import com.codeatlas.analysis.repository.SpringApiRepository;
import com.codeatlas.analyzer.spring.SpringRestApiExtractor;
import com.codeatlas.project.domain.Project;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpringApiAnalysisService {

    private final SourceFileRepository sourceFileRepository;
    private final SpringApiRepository springApiRepository;
    private final ApiFlowRepository apiFlowRepository;
    private final SpringRestApiExtractor springRestApiExtractor;

    public SpringApiAnalysisService(
            SourceFileRepository sourceFileRepository,
            SpringApiRepository springApiRepository,
            ApiFlowRepository apiFlowRepository,
            SpringRestApiExtractor springRestApiExtractor
    ) {
        this.sourceFileRepository = sourceFileRepository;
        this.springApiRepository = springApiRepository;
        this.apiFlowRepository = apiFlowRepository;
        this.springRestApiExtractor = springRestApiExtractor;
    }

    @Transactional
    public long extract(Project project) {
        springApiRepository.deleteByProjectId(project.getId());

        List<SpringApi> springApis = sourceFileRepository.findByProjectIdOrderByRelativePath(project.getId()).stream()
                .filter(sourceFile -> sourceFile.getType() == SourceFileType.JAVA)
                .flatMap(sourceFile -> springRestApiExtractor.extract(Path.of(sourceFile.getPath())).stream())
                .map(api -> SpringApi.create(
                        project,
                        api.httpMethod(),
                        api.path(),
                        api.controllerClassName(),
                        api.methodName(),
                        api.requestDtoName(),
                        api.responseDtoName(),
                        api.sourceFilePath(),
                        api.lineNumber()
                ))
                .toList();

        springApiRepository.saveAll(springApis);
        return springApis.size();
    }

    @Transactional(readOnly = true)
    public List<SpringApiResponse> findByProjectId(Long projectId) {
        return springApiRepository.findByProjectIdOrderByPathAscHttpMethodAsc(projectId).stream()
                .map(SpringApiResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpringApiDetailResponse getDetail(Long projectId, Long apiId) {
        SpringApi springApi = springApiRepository.findById(apiId)
                .filter(api -> api.getProject().getId().equals(projectId))
                .orElseThrow(() -> new IllegalArgumentException("Spring API not found: " + apiId));

        List<ApiFlowResponse> flows = apiFlowRepository
                .findByProjectIdOrderByApiPathAscHttpMethodAscMapperStatementIdAsc(projectId)
                .stream()
                .filter(flow -> flow.getHttpMethod().equals(springApi.getHttpMethod()))
                .filter(flow -> flow.getApiPath().equals(springApi.getPath()))
                .map(ApiFlowResponse::from)
                .toList();

        return new SpringApiDetailResponse(SpringApiResponse.from(springApi), flows);
    }
}
