package com.codeatlas.project.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ZipProjectExtractor {

    public void extract(MultipartFile zipFile, Path targetDirectory) {
        try {
            targetDirectory = targetDirectory.toAbsolutePath().normalize();
            Files.createDirectories(targetDirectory);
            try (InputStream inputStream = zipFile.getInputStream();
                 ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    extractEntry(zipInputStream, entry, targetDirectory);
                    zipInputStream.closeEntry();
                }
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to extract uploaded ZIP file.", exception);
        }
    }

    private void extractEntry(ZipInputStream zipInputStream, ZipEntry entry, Path targetDirectory) throws IOException {
        Path targetPath = targetDirectory.resolve(entry.getName()).normalize();
        if (!targetPath.startsWith(targetDirectory)) {
            throw new IllegalArgumentException("ZIP entry is outside target directory: " + entry.getName());
        }

        if (entry.isDirectory()) {
            Files.createDirectories(targetPath);
            return;
        }

        Path parent = targetPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.copy(zipInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
