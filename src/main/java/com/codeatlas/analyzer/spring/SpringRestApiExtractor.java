package com.codeatlas.analyzer.spring;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SpringRestApiExtractor {

    private static final Set<String> CONTROLLER_ANNOTATIONS = Set.of("RestController", "Controller");
    private static final Set<String> MAPPING_ANNOTATIONS = Set.of(
            "RequestMapping",
            "GetMapping",
            "PostMapping",
            "PutMapping",
            "DeleteMapping",
            "PatchMapping"
    );

    private final JavaParser javaParser = new JavaParser();

    public List<ExtractedSpringApi> extract(Path sourceFilePath) {
        try {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceFilePath);
            CompilationUnit compilationUnit = parseResult.getResult()
                    .orElseThrow(() -> new IllegalArgumentException("Failed to parse Java file: " + sourceFilePath));

            return compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                    .filter(this::isController)
                    .flatMap(controller -> extractControllerApis(controller, sourceFilePath).stream())
                    .toList();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to read Java file: " + sourceFilePath, exception);
        }
    }

    private boolean isController(ClassOrInterfaceDeclaration declaration) {
        return declaration.getAnnotations().stream()
                .map(annotation -> annotation.getName().getIdentifier())
                .anyMatch(CONTROLLER_ANNOTATIONS::contains);
    }

    private List<ExtractedSpringApi> extractControllerApis(
            ClassOrInterfaceDeclaration controller,
            Path sourceFilePath
    ) {
        String basePath = controller.getAnnotations().stream()
                .filter(annotation -> annotation.getNameAsString().equals("RequestMapping"))
                .findFirst()
                .flatMap(this::extractPath)
                .orElse("");

        List<ExtractedSpringApi> apis = new ArrayList<>();
        for (MethodDeclaration method : controller.getMethods()) {
            for (AnnotationExpr annotation : method.getAnnotations()) {
                if (!MAPPING_ANNOTATIONS.contains(annotation.getNameAsString())) {
                    continue;
                }

                String httpMethod = extractHttpMethod(annotation);
                String methodPath = extractPath(annotation).orElse("");
                apis.add(new ExtractedSpringApi(
                        httpMethod,
                        combinePaths(basePath, methodPath),
                        controller.getNameAsString(),
                        method.getNameAsString(),
                        extractRequestDtoName(method).orElse(null),
                        extractResponseDtoName(method).orElse(null),
                        sourceFilePath.toAbsolutePath().normalize().toString(),
                        method.getBegin().map(position -> position.line).orElse(null)
                ));
            }
        }
        return apis;
    }

    private String extractHttpMethod(AnnotationExpr annotation) {
        return switch (annotation.getNameAsString()) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "PutMapping" -> "PUT";
            case "DeleteMapping" -> "DELETE";
            case "PatchMapping" -> "PATCH";
            case "RequestMapping" -> extractRequestMappingMethod(annotation).orElse("REQUEST");
            default -> "REQUEST";
        };
    }

    private Optional<String> extractRequestMappingMethod(AnnotationExpr annotation) {
        if (!(annotation instanceof NormalAnnotationExpr normalAnnotationExpr)) {
            return Optional.empty();
        }

        return normalAnnotationExpr.getPairs().stream()
                .filter(pair -> pair.getNameAsString().equals("method"))
                .map(MemberValuePair::getValue)
                .map(Expression::toString)
                .map(value -> value.replace("RequestMethod.", ""))
                .map(value -> value.replaceAll("[{}\\s]", ""))
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    private Optional<String> extractPath(AnnotationExpr annotation) {
        if (annotation instanceof SingleMemberAnnotationExpr singleMemberAnnotationExpr) {
            return extractStringValue(singleMemberAnnotationExpr.getMemberValue());
        }

        if (annotation instanceof NormalAnnotationExpr normalAnnotationExpr) {
            return normalAnnotationExpr.getPairs().stream()
                    .filter(pair -> pair.getNameAsString().equals("value") || pair.getNameAsString().equals("path"))
                    .findFirst()
                    .flatMap(pair -> extractStringValue(pair.getValue()));
        }

        return Optional.empty();
    }

    private Optional<String> extractRequestDtoName(MethodDeclaration method) {
        return method.getParameters().stream()
                .filter(this::isRequestDtoParameter)
                .map(parameter -> simpleTypeName(parameter.getType().asString()))
                .findFirst();
    }

    private boolean isRequestDtoParameter(Parameter parameter) {
        boolean hasRequestBody = parameter.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getNameAsString().equals("RequestBody"));
        String typeName = simpleTypeName(parameter.getType().asString());
        return hasRequestBody || isDtoLike(typeName);
    }

    private Optional<String> extractResponseDtoName(MethodDeclaration method) {
        String typeName = simpleTypeName(method.getType().asString());
        if (typeName.equals("void") || typeName.equals("Void")) {
            return Optional.empty();
        }
        return isDtoLike(typeName) ? Optional.of(typeName) : Optional.empty();
    }

    private String simpleTypeName(String typeName) {
        String normalized = typeName.replace("?", "").replace("extends", "").trim();
        int genericStart = normalized.indexOf('<');
        if (genericStart >= 0 && normalized.endsWith(">")) {
            normalized = normalized.substring(genericStart + 1, normalized.length() - 1);
        }
        if (normalized.contains(",")) {
            normalized = normalized.substring(0, normalized.indexOf(','));
        }
        int packageSeparator = normalized.lastIndexOf('.');
        if (packageSeparator >= 0) {
            normalized = normalized.substring(packageSeparator + 1);
        }
        return normalized.trim();
    }

    private boolean isDtoLike(String typeName) {
        return typeName.endsWith("Request")
                || typeName.endsWith("Response")
                || typeName.endsWith("Dto")
                || typeName.endsWith("DTO");
    }

    private Optional<String> extractStringValue(Expression expression) {
        if (expression.isStringLiteralExpr()) {
            return Optional.of(expression.asStringLiteralExpr().asString());
        }

        if (expression instanceof ArrayInitializerExpr arrayInitializerExpr) {
            return arrayInitializerExpr.getValues().stream()
                    .findFirst()
                    .flatMap(this::extractStringValue);
        }

        return Optional.empty();
    }

    private String combinePaths(String basePath, String methodPath) {
        String combined = (normalizePath(basePath) + "/" + normalizePath(methodPath)).replaceAll("/{2,}", "/");
        if (combined.equals("/")) {
            return "/";
        }
        return combined.endsWith("/") ? combined.substring(0, combined.length() - 1) : combined;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank() || path.equals("/")) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}
