package com.codeatlas.analyzer.mybatis;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SqlTableExtractor {

    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "\\b(?:FROM|JOIN|UPDATE|INTO)\\s+([A-Z0-9_.$]+)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DELETE_PATTERN = Pattern.compile(
            "\\bDELETE\\s+FROM\\s+([A-Z0-9_.$]+)",
            Pattern.CASE_INSENSITIVE
    );

    public Set<String> extract(String sql) {
        Set<String> tableNames = new LinkedHashSet<>();
        addMatches(tableNames, TABLE_PATTERN, sql);
        addMatches(tableNames, DELETE_PATTERN, sql);
        return tableNames;
    }

    private void addMatches(Set<String> tableNames, Pattern pattern, String sql) {
        var matcher = pattern.matcher(sql);
        while (matcher.find()) {
            String tableName = sanitize(matcher.group(1));
            if (!tableName.isBlank()) {
                tableNames.add(tableName);
            }
        }
    }

    private String sanitize(String tableName) {
        return tableName
                .replaceAll("[,;)]$", "")
                .toUpperCase(Locale.ROOT);
    }
}
