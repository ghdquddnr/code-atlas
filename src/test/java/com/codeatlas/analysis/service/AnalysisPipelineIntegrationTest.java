package com.codeatlas.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeatlas.analysis.domain.AnalysisJobStatus;
import com.codeatlas.analysis.dto.AnalysisComparisonResponse;
import com.codeatlas.analysis.dto.AnalysisJobResponse;
import com.codeatlas.analysis.dto.AnalysisResultResetResponse;
import com.codeatlas.analysis.dto.JavaClassFieldResponse;
import com.codeatlas.analysis.dto.MyBatisStatementResponse;
import com.codeatlas.analysis.dto.ProjectDashboardResponse;
import com.codeatlas.analysis.dto.ProjectGraphResponse;
import com.codeatlas.analysis.dto.SourceFileResponse;
import com.codeatlas.analysis.dto.SpringApiDetailResponse;
import com.codeatlas.analysis.dto.SpringApiResponse;
import com.codeatlas.analysis.dto.TableUsageDetailResponse;
import com.codeatlas.analysis.dto.TableUsageResponse;
import com.codeatlas.analyzer.java.JavaClassCategory;
import com.codeatlas.chat.dto.QuestionAnswerResponse;
import com.codeatlas.chat.service.QuestionAnswerService;
import com.codeatlas.document.dto.GeneratedDocumentResponse;
import com.codeatlas.document.service.DocumentationService;
import com.codeatlas.project.dto.CreateProjectRequest;
import com.codeatlas.project.dto.ProjectDeleteResponse;
import com.codeatlas.project.dto.ProjectResponse;
import com.codeatlas.project.service.ProjectDeletionService;
import com.codeatlas.project.service.ProjectService;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AnalysisPipelineIntegrationTest {

    private final ProjectService projectService;
    private final AnalysisJobService analysisJobService;
    private final AnalysisResultCleanupService analysisResultCleanupService;
    private final SourceFileIndexingService sourceFileIndexingService;
    private final SpringApiAnalysisService springApiAnalysisService;
    private final MyBatisAnalysisService myBatisAnalysisService;
    private final ApiFlowMappingService apiFlowMappingService;
    private final ProjectDashboardService projectDashboardService;
    private final ProjectGraphService projectGraphService;
    private final AnalysisSnapshotService analysisSnapshotService;
    private final DocumentationService documentationService;
    private final QuestionAnswerService questionAnswerService;
    private final ProjectDeletionService projectDeletionService;

    @Autowired
    AnalysisPipelineIntegrationTest(
            ProjectService projectService,
            AnalysisJobService analysisJobService,
            AnalysisResultCleanupService analysisResultCleanupService,
            SourceFileIndexingService sourceFileIndexingService,
            SpringApiAnalysisService springApiAnalysisService,
            MyBatisAnalysisService myBatisAnalysisService,
            ApiFlowMappingService apiFlowMappingService,
            ProjectDashboardService projectDashboardService,
            ProjectGraphService projectGraphService,
            AnalysisSnapshotService analysisSnapshotService,
            DocumentationService documentationService,
            QuestionAnswerService questionAnswerService,
            ProjectDeletionService projectDeletionService
    ) {
        this.projectService = projectService;
        this.analysisJobService = analysisJobService;
        this.analysisResultCleanupService = analysisResultCleanupService;
        this.sourceFileIndexingService = sourceFileIndexingService;
        this.springApiAnalysisService = springApiAnalysisService;
        this.myBatisAnalysisService = myBatisAnalysisService;
        this.apiFlowMappingService = apiFlowMappingService;
        this.projectDashboardService = projectDashboardService;
        this.projectGraphService = projectGraphService;
        this.analysisSnapshotService = analysisSnapshotService;
        this.documentationService = documentationService;
        this.questionAnswerService = questionAnswerService;
        this.projectDeletionService = projectDeletionService;
    }

    @Test
    void analyzesSampleProjectEndToEnd() {
        String samplePath = Path.of("samples", "legacy-spring-mybatis")
                .toAbsolutePath()
                .normalize()
                .toString();

        ProjectResponse project = projectService.create(new CreateProjectRequest("sample", samplePath));

        AnalysisJobResponse analysisJob = analysisJobService.analyze(project.id());

        assertThat(analysisJob.status()).isEqualTo(AnalysisJobStatus.COMPLETED);
        List<SourceFileResponse> sourceFiles = sourceFileIndexingService.findByProjectId(project.id());
        assertThat(sourceFiles).hasSize(8);
        assertThat(sourceFiles)
                .extracting(SourceFileResponse::className, SourceFileResponse::classCategory)
                .contains(
                        org.assertj.core.groups.Tuple.tuple("OrderController", JavaClassCategory.CONTROLLER),
                        org.assertj.core.groups.Tuple.tuple("OrderService", JavaClassCategory.SERVICE),
                        org.assertj.core.groups.Tuple.tuple("OrderQueryService", JavaClassCategory.SERVICE),
                        org.assertj.core.groups.Tuple.tuple("OrderMapper", JavaClassCategory.MAPPER),
                        org.assertj.core.groups.Tuple.tuple("CreateOrderRequest", JavaClassCategory.DTO),
                        org.assertj.core.groups.Tuple.tuple("OrderResponse", JavaClassCategory.DTO),
                        org.assertj.core.groups.Tuple.tuple("UpdateOrderStatusRequest", JavaClassCategory.DTO)
                );
        assertThat(sourceFiles)
                .filteredOn(sourceFile -> "CreateOrderRequest".equals(sourceFile.className()))
                .singleElement()
                .satisfies(sourceFile -> assertThat(sourceFile.classFields())
                        .extracting(JavaClassFieldResponse::name, JavaClassFieldResponse::type)
                        .contains(
                                org.assertj.core.groups.Tuple.tuple("customerId", "Long"),
                                org.assertj.core.groups.Tuple.tuple("productIds", "List<Long>"),
                                org.assertj.core.groups.Tuple.tuple("memo", "String")
                        ));

        List<SpringApiResponse> apis = springApiAnalysisService.findByProjectId(project.id());
        assertThat(apis)
                .extracting(SpringApiResponse::httpMethod, SpringApiResponse::path)
                .contains(
                        org.assertj.core.groups.Tuple.tuple("POST", "/api/orders"),
                        org.assertj.core.groups.Tuple.tuple("GET", "/api/orders/{orderId}"),
                        org.assertj.core.groups.Tuple.tuple("GET", "/api/orders"),
                        org.assertj.core.groups.Tuple.tuple("PUT", "/api/orders/{orderId}/status"),
                        org.assertj.core.groups.Tuple.tuple("DELETE", "/api/orders/{orderId}")
                );
        assertThat(apis)
                .filteredOn(api -> api.methodName().equals("createOrder"))
                .singleElement()
                .satisfies(api -> {
                    assertThat(api.requestDtoName()).isEqualTo("CreateOrderRequest");
                    assertThat(api.responseDtoName()).isEqualTo("OrderResponse");
                });

        List<MyBatisStatementResponse> statements = myBatisAnalysisService.findStatements(project.id());
        assertThat(statements).hasSize(5);

        List<TableUsageResponse> tables = myBatisAnalysisService.findTables(project.id());
        assertThat(tables)
                .extracting(TableUsageResponse::tableName, TableUsageResponse::statementCount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("TB_ORDER", 5L),
                        org.assertj.core.groups.Tuple.tuple("TB_ORDER_ITEM", 1L),
                        org.assertj.core.groups.Tuple.tuple("TB_PRODUCT", 1L)
                );

        assertThat(apiFlowMappingService.findByProjectId(project.id()))
                .hasSize(7)
                .extracting(
                        flow -> flow.httpMethod() + " " + flow.apiPath(),
                        flow -> flow.serviceClassName() + "." + flow.serviceMethodName(),
                        flow -> flow.mapperStatementId()
                )
                .contains(
                        org.assertj.core.groups.Tuple.tuple(
                                "POST /api/orders",
                                "OrderService.createOrder",
                                "insertOrder"
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "POST /api/orders",
                                "OrderService.createOrder",
                                "selectOrderDetail"
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "GET /api/orders/{orderId}",
                                "OrderService.getOrder",
                                "selectOrderDetail"
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "PUT /api/orders/{orderId}/status",
                                "OrderService.updateOrderStatus",
                                "updateOrderStatus"
                        )
                );

        TableUsageDetailResponse tableUsageDetail = myBatisAnalysisService
                .getTableUsageDetail(project.id(), "tb_order");
        assertThat(tableUsageDetail.tableName()).isEqualTo("TB_ORDER");
        assertThat(tableUsageDetail.statements()).hasSize(5);
        assertThat(tableUsageDetail.flows()).hasSize(7);
        assertThat(tableUsageDetail.flows())
                .extracting(flow -> flow.httpMethod() + " " + flow.apiPath())
                .contains(
                        "POST /api/orders",
                        "GET /api/orders/{orderId}",
                        "PUT /api/orders/{orderId}/status",
                        "DELETE /api/orders/{orderId}"
                );

        Long createOrderApiId = apis.stream()
                .filter(api -> api.httpMethod().equals("POST"))
                .filter(api -> api.path().equals("/api/orders"))
                .findFirst()
                .orElseThrow()
                .id();
        SpringApiDetailResponse apiDetail = springApiAnalysisService.getDetail(project.id(), createOrderApiId);
        assertThat(apiDetail.api().controllerClassName()).isEqualTo("OrderController");
        assertThat(apiDetail.api().methodName()).isEqualTo("createOrder");
        assertThat(apiDetail.flows())
                .hasSize(2)
                .extracting(
                        flow -> flow.serviceClassName() + "." + flow.serviceMethodName(),
                        flow -> flow.mapperStatementId()
                )
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("OrderService.createOrder", "insertOrder"),
                        org.assertj.core.groups.Tuple.tuple("OrderService.createOrder", "selectOrderDetail")
                );

        GeneratedDocumentResponse onboardingDocument = documentationService.generateOnboardingDocument(project.id());
        assertThat(onboardingDocument.format()).isEqualTo("MARKDOWN");
        assertThat(onboardingDocument.evidenceCount()).isEqualTo(17);
        assertThat(onboardingDocument.content())
                .contains("# sample Onboarding Guide")
                .contains("Indexed Spring APIs: 5")
                .contains("MyBatis statements: 5")
                .contains("API flows: 7")
                .contains("`POST /api/orders`")
                .contains("`TB_ORDER`");

        GeneratedDocumentResponse apiDocument = documentationService.generateApiDocument(project.id());
        assertThat(apiDocument.content())
                .contains("# sample API Documentation")
                .contains("OrderController.createOrder()")
                .contains("OrderService.createOrder()")
                .contains("insertOrder");

        QuestionAnswerResponse answer = questionAnswerService.answer(project.id(), "TB_ORDER 어디서 사용돼?");
        assertThat(answer.confidence()).isEqualTo("HIGH");
        assertThat(answer.relatedTables()).contains("TB_ORDER");
        assertThat(answer.relatedApis())
                .contains("POST /api/orders", "GET /api/orders/{orderId}", "DELETE /api/orders/{orderId}");
        assertThat(answer.relatedMapperStatements())
                .contains("com.example.legacy.order.mapper.OrderMapper.insertOrder");
        assertThat(answer.evidence()).isNotEmpty();

        ProjectDashboardResponse dashboard = projectDashboardService.getDashboard(project.id());
        assertThat(dashboard.projectName()).isEqualTo("sample");
        assertThat(dashboard.latestAnalysisJob().status()).isEqualTo(AnalysisJobStatus.COMPLETED);
        assertThat(dashboard.sourceFileCount()).isEqualTo(8);
        assertThat(dashboard.springApiCount()).isEqualTo(5);
        assertThat(dashboard.myBatisStatementCount()).isEqualTo(5);
        assertThat(dashboard.apiFlowCount()).isEqualTo(7);
        assertThat(dashboard.tables())
                .extracting(TableUsageResponse::tableName)
                .containsExactly("TB_ORDER", "TB_ORDER_ITEM", "TB_PRODUCT");

        ProjectGraphResponse graph = projectGraphService.getGraph(project.id());
        assertThat(graph.nodes()).hasSize(29);
        assertThat(graph.edges()).hasSize(41);
        assertThat(graph.nodes())
                .extracting(node -> node.type() + ":" + node.label())
                .contains(
                        "API:POST /api/orders",
                        "CONTROLLER:OrderController",
                        "CONTROLLER_METHOD:createOrder()",
                        "SERVICE:OrderService",
                        "SERVICE_METHOD:createOrder()",
                        "SERVICE:OrderQueryService",
                        "SERVICE_METHOD:findOrderDetail()",
                        "MAPPER:com.example.legacy.order.mapper.OrderMapper",
                        "MAPPER_METHOD:insertOrder [INSERT]",
                        "TABLE:TB_ORDER"
                );
        assertThat(graph.edges())
                .extracting(edge -> edge.source() + " -> " + edge.target())
                .contains(
                        "api:POST /api/orders -> controller:OrderController",
                        "controller:OrderController -> controller-method:OrderController.createOrder",
                        "controller-method:OrderController.createOrder -> service:OrderService",
                        "service:OrderService -> service-method:OrderService.createOrder",
                        "service-method:OrderService.createOrder -> mapper:com.example.legacy.order.mapper.OrderMapper",
                        "mapper:com.example.legacy.order.mapper.OrderMapper -> mapper-method:com.example.legacy.order.mapper.OrderMapper.insertOrder",
                        "mapper-method:com.example.legacy.order.mapper.OrderMapper.insertOrder -> table:TB_ORDER",
                        "service-method:OrderService.getOrder -> service:OrderQueryService",
                        "service:OrderQueryService -> service-method:OrderQueryService.findOrderDetail",
                        "service-method:OrderQueryService.findOrderDetail -> mapper:com.example.legacy.order.mapper.OrderMapper"
                );

        AnalysisJobResponse secondAnalysisJob = analysisJobService.analyze(project.id());
        assertThat(secondAnalysisJob.status()).isEqualTo(AnalysisJobStatus.COMPLETED);
        AnalysisComparisonResponse comparison = analysisSnapshotService.compareLatest(project.id());
        assertThat(comparison.baseJobId()).isEqualTo(analysisJob.id());
        assertThat(comparison.targetJobId()).isEqualTo(secondAnalysisJob.id());
        assertThat(comparison.apis().addedCount()).isZero();
        assertThat(comparison.sqlStatements().removedCount()).isZero();
        assertThat(comparison.dtos().added()).isEmpty();
        assertThat(comparison.flows().removed()).isEmpty();
        AnalysisComparisonResponse selectedComparison = analysisSnapshotService.compare(
                project.id(),
                comparison.baseSnapshotId(),
                comparison.targetSnapshotId()
        );
        assertThat(selectedComparison.targetJobId()).isEqualTo(secondAnalysisJob.id());

        AnalysisResultResetResponse reset = analysisResultCleanupService.reset(project.id());
        assertThat(reset.deletedSourceFileCount()).isEqualTo(8);
        assertThat(reset.deletedSpringApiCount()).isEqualTo(5);
        assertThat(reset.deletedMyBatisStatementCount()).isEqualTo(5);
        assertThat(reset.deletedApiFlowCount()).isEqualTo(7);

        ProjectDashboardResponse resetDashboard = projectDashboardService.getDashboard(project.id());
        assertThat(resetDashboard.sourceFileCount()).isZero();
        assertThat(resetDashboard.springApiCount()).isZero();
        assertThat(resetDashboard.myBatisStatementCount()).isZero();
        assertThat(resetDashboard.apiFlowCount()).isZero();
        assertThat(resetDashboard.latestAnalysisJob().status()).isEqualTo(AnalysisJobStatus.COMPLETED);

        AnalysisJobResponse reanalysisJob = analysisJobService.analyze(project.id());
        assertThat(reanalysisJob.status()).isEqualTo(AnalysisJobStatus.COMPLETED);
        ProjectDashboardResponse reanalyzedDashboard = projectDashboardService.getDashboard(project.id());
        assertThat(reanalyzedDashboard.sourceFileCount()).isEqualTo(8);
        assertThat(reanalyzedDashboard.springApiCount()).isEqualTo(5);
        assertThat(reanalyzedDashboard.myBatisStatementCount()).isEqualTo(5);
        assertThat(reanalyzedDashboard.apiFlowCount()).isEqualTo(7);

        ProjectDeleteResponse deleteResponse = projectDeletionService.delete(project.id());
        assertThat(deleteResponse.deletedSourceFileCount()).isEqualTo(8);
        assertThat(deleteResponse.deletedSpringApiCount()).isEqualTo(5);
        assertThat(deleteResponse.deletedMyBatisStatementCount()).isEqualTo(5);
        assertThat(deleteResponse.deletedApiFlowCount()).isEqualTo(7);
        assertThat(deleteResponse.deletedAnalysisJobCount()).isEqualTo(3);
        assertThatThrownBy(() -> projectService.getById(project.id()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void marksAnalysisJobAsFailedWhenSourcePathDoesNotExist() {
        String missingPath = Path.of("samples", "missing-project")
                .toAbsolutePath()
                .normalize()
                .toString();

        ProjectResponse project = projectService.create(new CreateProjectRequest("missing", missingPath));

        AnalysisJobResponse failedJob = analysisJobService.analyze(project.id());

        assertThat(failedJob.status()).isEqualTo(AnalysisJobStatus.FAILED);
        assertThat(failedJob.message()).contains("Source path is not a directory");
        assertThat(failedJob.startedAt()).isNotNull();
        assertThat(failedJob.finishedAt()).isNotNull();

        AnalysisJobResponse latestStatus = analysisJobService.getLatestStatus(project.id());
        assertThat(latestStatus.id()).isEqualTo(failedJob.id());
        assertThat(latestStatus.status()).isEqualTo(AnalysisJobStatus.FAILED);
    }
}
