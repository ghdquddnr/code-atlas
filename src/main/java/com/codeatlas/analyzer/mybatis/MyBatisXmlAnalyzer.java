package com.codeatlas.analyzer.mybatis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@Component
public class MyBatisXmlAnalyzer {

    private static final List<String> STATEMENT_TYPES = List.of("select", "insert", "update", "delete");

    private final SqlTableExtractor sqlTableExtractor;

    public MyBatisXmlAnalyzer(SqlTableExtractor sqlTableExtractor) {
        this.sqlTableExtractor = sqlTableExtractor;
    }

    public List<ExtractedMyBatisStatement> analyze(Path sourceFilePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setExpandEntityReferences(false);

            var document = factory.newDocumentBuilder().parse(sourceFilePath.toFile());
            Element mapper = document.getDocumentElement();
            String namespace = mapper.getAttribute("namespace");

            List<ExtractedMyBatisStatement> statements = new ArrayList<>();
            for (String statementType : STATEMENT_TYPES) {
                var nodes = mapper.getElementsByTagName(statementType);
                for (int index = 0; index < nodes.getLength(); index++) {
                    Node node = nodes.item(index);
                    if (node instanceof Element element) {
                        String sql = normalizeSql(element.getTextContent());
                        statements.add(new ExtractedMyBatisStatement(
                                namespace,
                                element.getAttribute("id"),
                                statementType.toUpperCase(),
                                sql,
                                sqlTableExtractor.extract(sql),
                                optionalAttribute(element, "parameterType"),
                                optionalAttribute(element, "resultType"),
                                sourceFilePath.toAbsolutePath().normalize().toString()
                        ));
                    }
                }
            }
            return statements;
        } catch (IOException | ParserConfigurationException | SAXException exception) {
            throw new IllegalArgumentException("Failed to parse MyBatis XML file: " + sourceFilePath, exception);
        }
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    private String optionalAttribute(Element element, String name) {
        String value = element.getAttribute(name);
        return value == null || value.isBlank() ? null : value;
    }
}
