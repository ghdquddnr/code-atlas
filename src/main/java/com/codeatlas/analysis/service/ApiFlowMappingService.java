package com.codeatlas.analysis.service;

import com.codeatlas.analysis.domain.ApiFlow;
import com.codeatlas.analysis.domain.MyBatisStatement;
import com.codeatlas.analysis.domain.SourceFileType;
import com.codeatlas.analysis.domain.SpringApi;
import com.codeatlas.analysis.dto.ApiFlowResponse;
import com.codeatlas.analysis.repository.ApiFlowRepository;
import com.codeatlas.analysis.repository.MyBatisStatementRepository;
import com.codeatlas.analysis.repository.SourceFileRepository;
import com.codeatlas.analysis.repository.SpringApiRepository;
import com.codeatlas.analyzer.java.ExtractedJavaClass;
import com.codeatlas.analyzer.java.ExtractedMethodCall;
import com.codeatlas.analyzer.java.JavaMethodCallExtractor;
import com.codeatlas.project.domain.Project;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiFlowMappingService {

    private final ApiFlowRepository apiFlowRepository;
    private final SpringApiRepository springApiRepository;
    private final MyBatisStatementRepository myBatisStatementRepository;
    private final SourceFileRepository sourceFileRepository;
    private final JavaMethodCallExtractor javaMethodCallExtractor;

    public ApiFlowMappingService(
            ApiFlowRepository apiFlowRepository,
            SpringApiRepository springApiRepository,
            MyBatisStatementRepository myBatisStatementRepository,
            SourceFileRepository sourceFileRepository,
            JavaMethodCallExtractor javaMethodCallExtractor
    ) {
        this.apiFlowRepository = apiFlowRepository;
        this.springApiRepository = springApiRepository;
        this.myBatisStatementRepository = myBatisStatementRepository;
        this.sourceFileRepository = sourceFileRepository;
        this.javaMethodCallExtractor = javaMethodCallExtractor;
    }

    @Transactional
    public long map(Project project) {
        apiFlowRepository.deleteByProjectId(project.getId());

        List<ExtractedJavaClass> javaClasses = sourceFileRepository.findByProjectIdOrderByRelativePath(project.getId())
                .stream()
                .filter(sourceFile -> sourceFile.getType() == SourceFileType.JAVA)
                .flatMap(sourceFile -> javaMethodCallExtractor.extract(Path.of(sourceFile.getPath())).stream())
                .toList();

        Map<String, ExtractedJavaClass> classByName = javaClasses.stream()
                .collect(Collectors.toMap(ExtractedJavaClass::className, Function.identity(), (left, right) -> left));
        List<MyBatisStatement> statements = myBatisStatementRepository
                .findByProjectIdOrderByNamespaceAscStatementIdAsc(project.getId());

        List<ApiFlow> flows = springApiRepository.findByProjectIdOrderByPathAscHttpMethodAsc(project.getId()).stream()
                .flatMap(api -> mapApi(project, api, classByName, statements).stream())
                .toList();

        apiFlowRepository.saveAll(flows);
        return flows.size();
    }

    @Transactional(readOnly = true)
    public List<ApiFlowResponse> findByProjectId(Long projectId) {
        return apiFlowRepository.findByProjectIdOrderByApiPathAscHttpMethodAscMapperStatementIdAsc(projectId)
                .stream()
                .map(ApiFlowResponse::from)
                .toList();
    }

    private List<ApiFlow> mapApi(
            Project project,
            SpringApi api,
            Map<String, ExtractedJavaClass> classByName,
            List<MyBatisStatement> statements
    ) {
        ExtractedJavaClass controllerClass = classByName.get(api.getControllerClassName());
        if (controllerClass == null) {
            return List.of();
        }

        List<ExtractedMethodCall> controllerCalls = controllerClass.methodCalls().stream()
                .filter(call -> call.callerMethodName().equals(api.getMethodName()))
                .toList();

        List<ApiFlow> serviceFlows = controllerCalls.stream()
                .filter(call -> isServiceCall(call, classByName))
                .flatMap(serviceCall -> mapServiceCall(project, api, serviceCall, classByName, statements).stream())
                .toList();

        List<ApiFlow> directMapperFlows = controllerCalls.stream()
                .filter(call -> isMapperCall(call, classByName))
                .flatMap(mapperCall -> findStatement(mapperCall, statements)
                        .map(statement -> ApiFlow.create(
                                project,
                                api.getHttpMethod(),
                                api.getPath(),
                                api.getControllerClassName(),
                                api.getMethodName(),
                                null,
                                null,
                                statement.getNamespace(),
                                statement.getStatementId(),
                                statement.getStatementType(),
                                mapperCall.ownerClassName() + "." + mapperCall.callerMethodName() + "->" + mapperCall.targetClassName() + "." + mapperCall.targetMethodName(),
                                statement.getTableNames()
                        ))
                        .stream())
                .toList();

        return java.util.stream.Stream.concat(serviceFlows.stream(), directMapperFlows.stream()).toList();
    }

    private List<ApiFlow> mapServiceCall(
            Project project,
            SpringApi api,
            ExtractedMethodCall serviceCall,
            Map<String, ExtractedJavaClass> classByName,
            List<MyBatisStatement> statements
    ) {
        ExtractedJavaClass serviceClass = classByName.get(serviceCall.targetClassName());
        if (serviceClass == null) {
            return List.of();
        }

        return findReachableMapperCalls(serviceClass, serviceCall.targetMethodName(), classByName, Set.of())
                .stream()
                .flatMap(mapperPath -> findStatement(mapperPath.mapperCall(), statements)
                        .map(statement -> ApiFlow.create(
                                project,
                                api.getHttpMethod(),
                                api.getPath(),
                                api.getControllerClassName(),
                                api.getMethodName(),
                                serviceCall.targetClassName(),
                                serviceCall.targetMethodName(),
                                statement.getNamespace(),
                                statement.getStatementId(),
                                statement.getStatementType(),
                                mapperPath.toSummary(),
                                statement.getTableNames()
                        ))
                        .stream())
                .toList();
    }

    private List<ReachableMapperCall> findReachableMapperCalls(
            ExtractedJavaClass serviceClass,
            String methodName,
            Map<String, ExtractedJavaClass> classByName,
            Set<String> visitedMethodKeys
    ) {
        String methodKey = serviceClass.className() + "#" + methodName;
        if (visitedMethodKeys.contains(methodKey)) {
            return List.of();
        }

        Set<String> nextVisitedMethodKeys = new java.util.LinkedHashSet<>(visitedMethodKeys);
        nextVisitedMethodKeys.add(methodKey);

        List<ExtractedMethodCall> directCalls = serviceClass.methodCalls().stream()
                .filter(call -> call.callerMethodName().equals(methodName))
                .toList();

        List<ExtractedMethodCall> mapperCalls = directCalls.stream()
                .filter(call -> isMapperCall(call, classByName))
                .toList();

        List<ReachableMapperCall> directMapperCalls = mapperCalls.stream()
                .map(call -> new ReachableMapperCall(call, List.of(call)))
                .toList();

        List<ReachableMapperCall> internalMapperCalls = directCalls.stream()
                .filter(call -> call.targetClassName().equals(serviceClass.className()))
                .flatMap(call -> findReachableMapperCalls(
                        serviceClass,
                        call.targetMethodName(),
                        classByName,
                        nextVisitedMethodKeys
                ).stream()
                        .map(mapperPath -> mapperPath.prepend(call)))
                .toList();

        List<ReachableMapperCall> serviceMapperCalls = directCalls.stream()
                .filter(call -> isServiceCall(call, classByName))
                .filter(call -> !call.targetClassName().equals(serviceClass.className()))
                .flatMap(call -> findReachableMapperCalls(
                        classByName.get(call.targetClassName()),
                        call.targetMethodName(),
                        classByName,
                        nextVisitedMethodKeys
                ).stream()
                        .map(mapperPath -> mapperPath.prepend(call)))
                .toList();

        return java.util.stream.Stream.concat(
                        java.util.stream.Stream.concat(directMapperCalls.stream(), internalMapperCalls.stream()),
                        serviceMapperCalls.stream()
                )
                .toList();
    }

    private boolean isServiceCall(ExtractedMethodCall call, Map<String, ExtractedJavaClass> classByName) {
        ExtractedJavaClass targetClass = classByName.get(call.targetClassName());
        return targetClass != null
                && (targetClass.hasAnnotation("Service") || targetClass.className().endsWith("Service"));
    }

    private boolean isMapperCall(ExtractedMethodCall call, Map<String, ExtractedJavaClass> classByName) {
        ExtractedJavaClass targetClass = classByName.get(call.targetClassName());
        return targetClass != null
                && (targetClass.hasAnnotation("Mapper") || targetClass.className().endsWith("Mapper"));
    }

    private Optional<MyBatisStatement> findStatement(ExtractedMethodCall mapperCall, List<MyBatisStatement> statements) {
        return statements.stream()
                .filter(statement -> statement.getNamespace().endsWith("." + mapperCall.targetClassName()))
                .filter(statement -> statement.getStatementId().equals(mapperCall.targetMethodName()))
                .findFirst();
    }

    private record ReachableMapperCall(ExtractedMethodCall mapperCall, List<ExtractedMethodCall> path) {

        private ReachableMapperCall prepend(ExtractedMethodCall call) {
            List<ExtractedMethodCall> nextPath = new java.util.ArrayList<>();
            nextPath.add(call);
            nextPath.addAll(path);
            return new ReachableMapperCall(mapperCall, List.copyOf(nextPath));
        }

        private String toSummary() {
            return path.stream()
                    .map(call -> call.ownerClassName() + "." + call.callerMethodName()
                            + "->" + call.targetClassName() + "." + call.targetMethodName())
                    .collect(Collectors.joining("\n"));
        }
    }
}
