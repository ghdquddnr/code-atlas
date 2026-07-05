package com.codeatlas.analyzer.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class JavaMethodCallExtractor {

    private final JavaParser javaParser = new JavaParser();

    public List<ExtractedJavaClass> extract(Path sourceFilePath) {
        try {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceFilePath);
            CompilationUnit compilationUnit = parseResult.getResult()
                    .orElseThrow(() -> new IllegalArgumentException("Failed to parse Java file: " + sourceFilePath));

            List<ExtractedJavaClass> extractedClasses = new ArrayList<>();
            extractedClasses.addAll(compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                    .map(declaration -> extractClass(compilationUnit, declaration, sourceFilePath))
                    .toList());
            extractedClasses.addAll(compilationUnit.findAll(RecordDeclaration.class).stream()
                    .map(declaration -> extractRecord(compilationUnit, declaration, sourceFilePath))
                    .toList());
            return extractedClasses;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to read Java file: " + sourceFilePath, exception);
        }
    }

    private ExtractedJavaClass extractClass(
            CompilationUnit compilationUnit,
            ClassOrInterfaceDeclaration declaration,
            Path sourceFilePath
    ) {
        Map<String, String> fieldTypes = extractFieldTypes(declaration);
        List<ExtractedJavaField> fields = fieldTypes.entrySet().stream()
                .map(entry -> new ExtractedJavaField(entry.getKey(), entry.getValue()))
                .toList();
        List<ExtractedMethodCall> methodCalls = declaration.getMethods().stream()
                .flatMap(method -> extractMethodCalls(declaration, method, fieldTypes).stream())
                .toList();

        Set<String> annotations = declaration.getAnnotations().stream()
                .map(annotation -> annotation.getName().getIdentifier())
                .collect(Collectors.toSet());

        return new ExtractedJavaClass(
                compilationUnit.getPackageDeclaration()
                        .map(packageDeclaration -> packageDeclaration.getName().asString())
                        .orElse(""),
                declaration.getNameAsString(),
                classify(declaration, sourceFilePath),
                annotations,
                fields,
                fieldTypes,
                methodCalls,
                sourceFilePath.toAbsolutePath().normalize().toString()
        );
    }

    private ExtractedJavaClass extractRecord(
            CompilationUnit compilationUnit,
            RecordDeclaration declaration,
            Path sourceFilePath
    ) {
        Set<String> annotations = declaration.getAnnotations().stream()
                .map(annotation -> annotation.getName().getIdentifier())
                .collect(Collectors.toSet());
        List<ExtractedMethodCall> methodCalls = declaration.getMethods().stream()
                .flatMap(method -> extractMethodCalls(declaration.getNameAsString(), method, Map.of()).stream())
                .toList();

        return new ExtractedJavaClass(
                compilationUnit.getPackageDeclaration()
                        .map(packageDeclaration -> packageDeclaration.getName().asString())
                        .orElse(""),
                declaration.getNameAsString(),
                classify(declaration.getNameAsString(), annotations, hasScheduledMethod(declaration.getMethods()), sourceFilePath),
                annotations,
                declaration.getParameters().stream()
                        .map(parameter -> new ExtractedJavaField(parameter.getNameAsString(), parameter.getType().asString()))
                        .toList(),
                Map.of(),
                methodCalls,
                sourceFilePath.toAbsolutePath().normalize().toString()
        );
    }

    private Map<String, String> extractFieldTypes(ClassOrInterfaceDeclaration declaration) {
        Map<String, String> fieldTypes = new LinkedHashMap<>();
        for (FieldDeclaration field : declaration.getFields()) {
            field.getVariables().forEach(variable -> fieldTypes.put(
                    variable.getNameAsString(),
                    variable.getType().asString()
            ));
        }
        return fieldTypes;
    }

    private List<ExtractedMethodCall> extractMethodCalls(
            ClassOrInterfaceDeclaration declaration,
            MethodDeclaration method,
            Map<String, String> fieldTypes
    ) {
        return method.findAll(MethodCallExpr.class).stream()
                .map(methodCall -> toExtractedMethodCall(declaration.getNameAsString(), method, fieldTypes, methodCall))
                .filter(methodCall -> methodCall.targetClassName() != null)
                .toList();
    }

    private List<ExtractedMethodCall> extractMethodCalls(
            String className,
            MethodDeclaration method,
            Map<String, String> fieldTypes
    ) {
        return method.findAll(MethodCallExpr.class).stream()
                .map(methodCall -> toExtractedMethodCall(className, method, fieldTypes, methodCall))
                .filter(methodCall -> methodCall.targetClassName() != null)
                .toList();
    }

    private ExtractedMethodCall toExtractedMethodCall(
            String className,
            MethodDeclaration method,
            Map<String, String> fieldTypes,
            MethodCallExpr methodCall
    ) {
        if (methodCall.getScope().isEmpty()) {
            return new ExtractedMethodCall(
                    className,
                    method.getNameAsString(),
                    null,
                    className,
                    methodCall.getNameAsString()
            );
        }

        String targetFieldName = methodCall.getScope().orElseThrow().toString();
        return new ExtractedMethodCall(
                className,
                method.getNameAsString(),
                targetFieldName,
                fieldTypes.get(targetFieldName),
                methodCall.getNameAsString()
        );
    }

    private JavaClassCategory classify(ClassOrInterfaceDeclaration declaration, Path sourceFilePath) {
        Set<String> annotations = declaration.getAnnotations().stream()
                .map(annotation -> annotation.getName().getIdentifier())
                .collect(Collectors.toSet());
        return classify(
                declaration.getNameAsString(),
                annotations,
                hasScheduledMethod(declaration.getMethods()),
                sourceFilePath
        );
    }

    private JavaClassCategory classify(
            String className,
            Set<String> annotations,
            boolean hasScheduledMethod,
            Path sourceFilePath
    ) {
        String normalizedPath = sourceFilePath.toString().replace('\\', '/').toLowerCase();

        if (annotations.contains("RestController") || annotations.contains("Controller")) {
            return JavaClassCategory.CONTROLLER;
        }
        if (annotations.contains("Service")) {
            return JavaClassCategory.SERVICE;
        }
        if (annotations.contains("Repository")) {
            return JavaClassCategory.REPOSITORY;
        }
        if (annotations.contains("Mapper")) {
            return JavaClassCategory.MAPPER;
        }
        if (annotations.contains("Entity")) {
            return JavaClassCategory.ENTITY;
        }
        if (annotations.contains("Configuration")) {
            return JavaClassCategory.CONFIGURATION;
        }
        if (hasScheduledMethod) {
            return JavaClassCategory.SCHEDULER;
        }
        if (className.endsWith("Job") || className.endsWith("Batch") || normalizedPath.contains("/batch/")) {
            return JavaClassCategory.BATCH;
        }
        if (annotations.contains("Component")) {
            return JavaClassCategory.COMPONENT;
        }
        if (normalizedPath.contains("/test/") || className.endsWith("Test")) {
            return JavaClassCategory.TEST;
        }
        if (className.endsWith("Request")
                || className.endsWith("Response")
                || className.endsWith("Dto")
                || className.endsWith("DTO")
                || normalizedPath.contains("/dto/")) {
            return JavaClassCategory.DTO;
        }
        if (className.endsWith("Utils") || className.endsWith("Util") || normalizedPath.contains("/util/")) {
            return JavaClassCategory.UTILITY;
        }
        return JavaClassCategory.UNKNOWN;
    }

    private boolean hasScheduledMethod(List<MethodDeclaration> methods) {
        return methods.stream()
                .flatMap(method -> method.getAnnotations().stream())
                .anyMatch(annotation -> annotation.getNameAsString().equals("Scheduled"));
    }
}
