package com.codeatlas.analyzer.mybatis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SqlTableExtractorTest {

    private final SqlTableExtractor extractor = new SqlTableExtractor();

    @Test
    void extractsTablesFromCommonSqlStatements() {
        String sql = """
                SELECT *
                FROM TB_ORDER O
                JOIN TB_ORDER_ITEM OI ON O.ORDER_ID = OI.ORDER_ID
                JOIN TB_PRODUCT P ON OI.PRODUCT_ID = P.PRODUCT_ID
                WHERE O.ORDER_ID = #{orderId}
                """;

        assertThat(extractor.extract(sql))
                .containsExactly("TB_ORDER", "TB_ORDER_ITEM", "TB_PRODUCT");
    }

    @Test
    void extractsTablesFromInsertUpdateDelete() {
        assertThat(extractor.extract("INSERT INTO TB_ORDER (ORDER_ID) VALUES (1)"))
                .containsExactly("TB_ORDER");
        assertThat(extractor.extract("UPDATE TB_ORDER SET ORDER_STATUS = 'DONE'"))
                .containsExactly("TB_ORDER");
        assertThat(extractor.extract("DELETE FROM TB_ORDER WHERE ORDER_ID = 1"))
                .containsExactly("TB_ORDER");
    }
}
