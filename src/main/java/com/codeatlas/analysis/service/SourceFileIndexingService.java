package com.codeatlas.analysis.service;

import com.codeatlas.analysis.domain.SourceFile;
import com.codeatlas.analysis.domain.SourceFileType;
import com.codeatlas.analysis.dto.SourceFileResponse;
import com.codeatlas.analysis.repository.SourceFileRepository;
import com.codeatlas.analyzer.java.ExtractedJavaClass;
import com.codeatlas.analyzer.java.JavaMethodCallExtractor;
import com.codeatlas.project.domain.Project;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SourceFileIndexingService {

    private final SourceFileRepository sourceFileRepository;
    private final JavaMethodCallExtractor javaMethodCallExtractor;

    public SourceFileIndexingService(
            SourceFileRepository sourceFileRepository,
            JavaMethodCallExtractor javaMethodCallExtractor
    ) {
        this.sourceFileRepository = sourceFileRepository;
        this.javaMethodCallExtractor = javaMethodCallExtractor;
    }

    @Transactional
    public long index(Project project) {
        Path rootPath = Path.of(project.getSourcePath()).toAbsolutePath().normalize();
        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Source path is not a directory: " + project.getSourcePath());
        }

        sourceFileRepository.deleteByProjectId(project.getId());

        try (var paths = Files.walk(rootPath)) {
            List<SourceFile> sourceFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFile)
                    .map(path -> toSourceFile(project, rootPath, path))
                    .toList();

            sourceFileRepository.saveAll(sourceFiles);
            return sourceFiles.size();
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to index source files.", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<SourceFileResponse> findByProjectId(Long projectId) {
        return sourceFileRepository.findByProjectIdOrderByRelativePath(projectId).stream()
                .map(SourceFileResponse::from)
                .toList();
    }

    private boolean isSupportedFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".java") || fileName.endsWith(".xml");
    }

    private SourceFile toSourceFile(Project project, Path rootPath, Path path) {
        Path absolutePath = path.toAbsolutePath().normalize();
        String relativePath = rootPath.relativize(absolutePath).toString().replace('\\', '/');
        SourceFileType type = classify(absolutePath, relativePath);

        try {
            JavaClassMetadata javaClassMetadata = extractJavaClassMetadata(type, absolutePath);
            return SourceFile.create(
                    project,
                    absolutePath.toString(),
                    relativePath,
                    type,
                    Files.size(path),
                    javaClassMetadata.packageName(),
                    javaClassMetadata.className(),
                    javaClassMetadata.category(),
                    javaClassMetadata.fieldSummary()
            );
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read file size: " + path, exception);
        }
    }

    private SourceFileType classify(Path absolutePath, String relativePath) {
        String normalizedPath = relativePath.toLowerCase();
        if (normalizedPath.endsWith(".java")) {
            return SourceFileType.JAVA;
        }
        if (normalizedPath.endsWith(".xml")) {
            try {
                try (var reader = Files.newBufferedReader(absolutePath)) {
                    String line;
                    int count = 0;
                    while ((line = reader.readLine()) != null && count < 20) {
                        if (line.contains("<mapper") || line.contains("mybatis.org")) {
                            return SourceFileType.MYBATIS_XML;
                        }
                        count++;
                    }
                }
            } catch (IOException exception) {
                if (normalizedPath.startsWith("src/main/resources/")) {
                    return SourceFileType.MYBATIS_XML;
                }
            }
            return SourceFileType.XML;
        }
        return SourceFileType.OTHER;
    }

    private JavaClassMetadata extractJavaClassMetadata(SourceFileType type, Path path) {
        if (type != SourceFileType.JAVA) {
            return JavaClassMetadata.empty();
        }

        try {
            return javaMethodCallExtractor.extract(path).stream()
                    .findFirst()
                    .map(javaClass -> new JavaClassMetadata(
                            javaClass.packageName(),
                            javaClass.className(),
                            javaClass.category(),
                            javaClass.fields().stream()
                                    .map(com.codeatlas.analyzer.java.ExtractedJavaField::toSummary)
                                    .collect(java.util.stream.Collectors.joining("\n"))
                    ))
                    .orElse(JavaClassMetadata.empty());
        } catch (IllegalArgumentException exception) {
            return JavaClassMetadata.empty();
        }
    }

    private record JavaClassMetadata(
            String packageName,
            String className,
            com.codeatlas.analyzer.java.JavaClassCategory category,
            String fieldSummary
    ) {
        private static JavaClassMetadata empty() {
            return new JavaClassMetadata(null, null, null, null);
        }
    }
}
