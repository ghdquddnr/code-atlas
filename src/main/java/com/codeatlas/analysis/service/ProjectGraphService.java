package com.codeatlas.analysis.service;

import com.codeatlas.analysis.dto.ApiFlowResponse;
import com.codeatlas.analysis.dto.GraphEdgeResponse;
import com.codeatlas.analysis.dto.GraphNodeResponse;
import com.codeatlas.analysis.dto.MethodCallStepResponse;
import com.codeatlas.analysis.dto.ProjectGraphResponse;
import com.codeatlas.project.service.ProjectService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectGraphService {

    private final ProjectService projectService;
    private final ApiFlowMappingService apiFlowMappingService;

    public ProjectGraphService(ProjectService projectService, ApiFlowMappingService apiFlowMappingService) {
        this.projectService = projectService;
        this.apiFlowMappingService = apiFlowMappingService;
    }

    @Transactional(readOnly = true)
    public ProjectGraphResponse getGraph(Long projectId) {
        projectService.findProject(projectId);

        Map<String, GraphNodeResponse> nodes = new LinkedHashMap<>();
        Map<String, GraphEdgeResponse> edges = new LinkedHashMap<>();

        for (ApiFlowResponse flow : apiFlowMappingService.findByProjectId(projectId)) {
            String apiNodeId = "api:" + flow.httpMethod() + " " + flow.apiPath();
            String controllerNodeId = "controller:" + flow.controllerClassName();
            String controllerMethodNodeId = "controller-method:" + flow.controllerClassName() + "." + flow.controllerMethodName();
            String mapperMethodNodeId = "mapper-method:" + flow.mapperNamespace() + "." + flow.mapperStatementId();

            putNode(nodes, apiNodeId, "API", flow.httpMethod() + " " + flow.apiPath());
            putNode(nodes, controllerNodeId, "CONTROLLER", flow.controllerClassName());
            putNode(nodes, controllerMethodNodeId, "CONTROLLER_METHOD", flow.controllerMethodName() + "()");

            putEdge(edges, apiNodeId, controllerNodeId, "handled_by");
            putEdge(edges, controllerNodeId, controllerMethodNodeId, "declares");

            if (flow.methodCallPath().isEmpty()) {
                String serviceNodeId = serviceNodeId(flow.serviceClassName());
                String serviceMethodNodeId = serviceMethodNodeId(flow.serviceClassName(), flow.serviceMethodName());
                String mapperNodeId = mapperNodeId(flow.mapperNamespace());

                putNode(nodes, serviceNodeId, "SERVICE", flow.serviceClassName());
                putNode(nodes, serviceMethodNodeId, "SERVICE_METHOD", flow.serviceMethodName() + "()");
                putNode(nodes, mapperNodeId, "MAPPER", flow.mapperNamespace());
                putNode(nodes, mapperMethodNodeId, "MAPPER_METHOD", flow.mapperStatementId() + " [" + flow.mapperStatementType() + "]");

                putEdge(edges, controllerMethodNodeId, serviceNodeId, "calls");
                putEdge(edges, serviceNodeId, serviceMethodNodeId, "declares");
                putEdge(edges, serviceMethodNodeId, mapperNodeId, "calls");
                putEdge(edges, mapperNodeId, mapperMethodNodeId, "declares");
            } else {
                putMethodCallPath(nodes, edges, controllerMethodNodeId, flow);
            }

            for (String tableName : flow.tableNames()) {
                String tableNodeId = "table:" + tableName;
                putNode(nodes, tableNodeId, "TABLE", tableName);
                putEdge(edges, mapperMethodNodeId, tableNodeId, "uses");
            }
        }

        return new ProjectGraphResponse(projectId, List.copyOf(nodes.values()), List.copyOf(edges.values()));
    }

    private void putNode(Map<String, GraphNodeResponse> nodes, String id, String type, String label) {
        nodes.putIfAbsent(id, new GraphNodeResponse(id, type, label));
    }

    private void putEdge(Map<String, GraphEdgeResponse> edges, String source, String target, String label) {
        String id = source + "->" + target + ":" + label;
        edges.putIfAbsent(id, new GraphEdgeResponse(id, source, target, label));
    }

    private void putMethodCallPath(
            Map<String, GraphNodeResponse> nodes,
            Map<String, GraphEdgeResponse> edges,
            String controllerMethodNodeId,
            ApiFlowResponse flow
    ) {
        for (MethodCallStepResponse step : flow.methodCallPath()) {
            String sourceClassNodeId = serviceNodeId(step.sourceClassName());
            String sourceMethodNodeId = serviceMethodNodeId(step.sourceClassName(), step.sourceMethodName());
            putNode(nodes, sourceClassNodeId, "SERVICE", step.sourceClassName());
            putNode(nodes, sourceMethodNodeId, "SERVICE_METHOD", step.sourceMethodName() + "()");
            putEdge(edges, sourceClassNodeId, sourceMethodNodeId, "declares");

            if (step.sourceClassName().equals(flow.serviceClassName())
                    && step.sourceMethodName().equals(flow.serviceMethodName())) {
                putEdge(edges, controllerMethodNodeId, sourceClassNodeId, "calls");
            }

            if (isMapperStep(step, flow)) {
                String targetClassNodeId = mapperNodeId(flow.mapperNamespace());
                String targetMethodNodeId = mapperMethodNodeId(flow.mapperNamespace(), step.targetMethodName());
                putNode(nodes, targetClassNodeId, "MAPPER", flow.mapperNamespace());
                putNode(nodes, targetMethodNodeId, "MAPPER_METHOD", step.targetMethodName() + " [" + flow.mapperStatementType() + "]");
                putEdge(edges, sourceMethodNodeId, targetClassNodeId, "calls");
                putEdge(edges, targetClassNodeId, targetMethodNodeId, "declares");
            } else {
                String targetClassNodeId = serviceNodeId(step.targetClassName());
                String targetMethodNodeId = serviceMethodNodeId(step.targetClassName(), step.targetMethodName());
                putNode(nodes, targetClassNodeId, "SERVICE", step.targetClassName());
                putNode(nodes, targetMethodNodeId, "SERVICE_METHOD", step.targetMethodName() + "()");
                putEdge(edges, sourceMethodNodeId, targetClassNodeId, "calls");
                putEdge(edges, targetClassNodeId, targetMethodNodeId, "declares");
            }
        }
    }

    private boolean isMapperStep(MethodCallStepResponse step, ApiFlowResponse flow) {
        return flow.mapperNamespace().endsWith("." + step.targetClassName())
                && flow.mapperStatementId().equals(step.targetMethodName());
    }

    private String serviceNodeId(String className) {
        return "service:" + className;
    }

    private String serviceMethodNodeId(String className, String methodName) {
        return "service-method:" + className + "." + methodName;
    }

    private String mapperNodeId(String namespace) {
        return "mapper:" + namespace;
    }

    private String mapperMethodNodeId(String namespace, String statementId) {
        return "mapper-method:" + namespace + "." + statementId;
    }
}
