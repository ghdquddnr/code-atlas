package com.codeatlas.analyzer.java;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ExtractedJavaClass(
        String packageName,
        String className,
        JavaClassCategory category,
        Set<String> annotations,
        List<ExtractedJavaField> fields,
        Map<String, String> fieldTypes,
        List<ExtractedMethodCall> methodCalls,
        String sourceFilePath
) {

    public boolean hasAnnotation(String annotationName) {
        return annotations.contains(annotationName);
    }
}
