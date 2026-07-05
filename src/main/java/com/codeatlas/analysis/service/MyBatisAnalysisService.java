package com.codeatlas.analysis.service;

import com.codeatlas.analysis.domain.MyBatisStatement;
import com.codeatlas.analysis.domain.SourceFileType;
import com.codeatlas.analysis.dto.MyBatisStatementResponse;
import com.codeatlas.analysis.dto.TableUsageDetailResponse;
import com.codeatlas.analysis.dto.TableUsageResponse;
import com.codeatlas.analysis.repository.ApiFlowRepository;
import com.codeatlas.analysis.repository.MyBatisStatementRepository;
import com.codeatlas.analysis.repository.SourceFileRepository;
import com.codeatlas.analyzer.mybatis.MyBatisXmlAnalyzer;
import com.codeatlas.project.domain.Project;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyBatisAnalysisService {

    private final SourceFileRepository sourceFileRepository;
    private final MyBatisStatementRepository myBatisStatementRepository;
    private final ApiFlowRepository apiFlowRepository;
    private final MyBatisXmlAnalyzer myBatisXmlAnalyzer;

    public MyBatisAnalysisService(
            SourceFileRepository sourceFileRepository,
            MyBatisStatementRepository myBatisStatementRepository,
            ApiFlowRepository apiFlowRepository,
            MyBatisXmlAnalyzer myBatisXmlAnalyzer
    ) {
        this.sourceFileRepository = sourceFileRepository;
        this.myBatisStatementRepository = myBatisStatementRepository;
        this.apiFlowRepository = apiFlowRepository;
        this.myBatisXmlAnalyzer = myBatisXmlAnalyzer;
    }

    @Transactional
    public long extract(Project project) {
        myBatisStatementRepository.deleteByProjectId(project.getId());

        List<MyBatisStatement> statements = sourceFileRepository.findByProjectIdOrderByRelativePath(project.getId())
                .stream()
                .filter(sourceFile -> sourceFile.getType() == SourceFileType.MYBATIS_XML)
                .flatMap(sourceFile -> myBatisXmlAnalyzer.analyze(Path.of(sourceFile.getPath())).stream())
                .map(statement -> MyBatisStatement.create(
                        project,
                        statement.namespace(),
                        statement.statementId(),
                        statement.statementType(),
                        statement.sql(),
                        statement.tableNames(),
                        statement.parameterType(),
                        statement.resultType(),
                        statement.sourceFilePath()
                ))
                .toList();

        myBatisStatementRepository.saveAll(statements);
        return statements.size();
    }

    @Transactional(readOnly = true)
    public List<MyBatisStatementResponse> findStatements(Long projectId) {
        return myBatisStatementRepository.findByProjectIdOrderByNamespaceAscStatementIdAsc(projectId).stream()
                .map(MyBatisStatementResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TableUsageResponse> findTables(Long projectId) {
        Map<String, Long> usageCounts = new TreeMap<>();
        for (MyBatisStatement statement : myBatisStatementRepository.findByProjectIdOrderByNamespaceAscStatementIdAsc(projectId)) {
            for (String tableName : statement.getTableNames()) {
                usageCounts.merge(tableName, 1L, Long::sum);
            }
        }
        return usageCounts.entrySet().stream()
                .map(entry -> new TableUsageResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TableUsageDetailResponse getTableUsageDetail(Long projectId, String tableName) {
        String normalizedTableName = tableName.toUpperCase(Locale.ROOT);
        List<MyBatisStatementResponse> statements = myBatisStatementRepository
                .findByProjectIdOrderByNamespaceAscStatementIdAsc(projectId)
                .stream()
                .filter(statement -> statement.getTableNames().contains(normalizedTableName))
                .map(MyBatisStatementResponse::from)
                .toList();

        List<com.codeatlas.analysis.dto.ApiFlowResponse> flows = apiFlowRepository
                .findByProjectIdOrderByApiPathAscHttpMethodAscMapperStatementIdAsc(projectId)
                .stream()
                .filter(flow -> flow.getTableNames().contains(normalizedTableName))
                .map(com.codeatlas.analysis.dto.ApiFlowResponse::from)
                .toList();

        return new TableUsageDetailResponse(normalizedTableName, statements, flows);
    }
}
