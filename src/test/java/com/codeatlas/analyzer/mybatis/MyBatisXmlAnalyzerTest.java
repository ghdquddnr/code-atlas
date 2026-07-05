package com.codeatlas.analyzer.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class MyBatisXmlAnalyzerTest {

    private final MyBatisXmlAnalyzer analyzer = new MyBatisXmlAnalyzer(new SqlTableExtractor());

    @Test
    void extractsStatementsFromSampleMapperXml() {
        Path sourceFilePath = Path.of(
                "samples",
                "legacy-spring-mybatis",
                "src",
                "main",
                "resources",
                "mapper",
                "order",
                "OrderMapper.xml"
        );

        List<ExtractedMyBatisStatement> statements = analyzer.analyze(sourceFilePath);

        assertThat(statements)
                .extracting(
                        ExtractedMyBatisStatement::namespace,
                        ExtractedMyBatisStatement::statementId,
                        ExtractedMyBatisStatement::statementType
                )
                .containsExactlyInAnyOrder(
                        tuple("com.example.legacy.order.mapper.OrderMapper", "insertOrder", "INSERT"),
                        tuple("com.example.legacy.order.mapper.OrderMapper", "selectOrderDetail", "SELECT"),
                        tuple("com.example.legacy.order.mapper.OrderMapper", "selectRecentOrders", "SELECT"),
                        tuple("com.example.legacy.order.mapper.OrderMapper", "updateOrderStatus", "UPDATE"),
                        tuple("com.example.legacy.order.mapper.OrderMapper", "deleteOrder", "DELETE")
                );

        ExtractedMyBatisStatement selectOrderDetail = statements.stream()
                .filter(statement -> statement.statementId().equals("selectOrderDetail"))
                .findFirst()
                .orElseThrow();

        assertThat(selectOrderDetail.tableNames())
                .containsExactly("TB_ORDER", "TB_ORDER_ITEM", "TB_PRODUCT");
        assertThat(selectOrderDetail.parameterType()).isEqualTo("long");
        assertThat(selectOrderDetail.resultType()).isEqualTo("com.example.legacy.order.dto.OrderResponse");
        assertThat(selectOrderDetail.sourceFilePath()).endsWith("OrderMapper.xml");
    }
}
