package com.codeatlas.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ZipProjectExtractorTest {

    private final ZipProjectExtractor extractor = new ZipProjectExtractor();

    @TempDir
    Path tempDirectory;

    @Test
    void extractsZipFileIntoTargetDirectory() throws IOException {
        MockMultipartFile zipFile = zipFile("sample.zip",
                entry("src/main/java/SampleController.java", "class SampleController {}"),
                entry("src/main/resources/mapper/SampleMapper.xml", "<mapper />")
        );

        Path targetDirectory = tempDirectory.resolve("project");
        extractor.extract(zipFile, targetDirectory);

        assertThat(targetDirectory.resolve("src/main/java/SampleController.java"))
                .exists()
                .hasContent("class SampleController {}");
        assertThat(targetDirectory.resolve("src/main/resources/mapper/SampleMapper.xml"))
                .exists()
                .hasContent("<mapper />");
    }

    @Test
    void rejectsZipEntriesOutsideTargetDirectory() throws IOException {
        MockMultipartFile zipFile = zipFile("bad.zip",
                entry("../escape.txt", "escape")
        );

        Path targetDirectory = tempDirectory.resolve("project");

        assertThatThrownBy(() -> extractor.extract(zipFile, targetDirectory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside target directory");
        assertThat(tempDirectory.resolve("escape.txt")).doesNotExist();
    }

    private MockMultipartFile zipFile(String fileName, TestZipEntry... entries) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (TestZipEntry entry : entries) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.name()));
                zipOutputStream.write(entry.content().getBytes());
                zipOutputStream.closeEntry();
            }
        }
        return new MockMultipartFile("file", fileName, "application/zip", outputStream.toByteArray());
    }

    private TestZipEntry entry(String name, String content) {
        return new TestZipEntry(name, content);
    }

    private record TestZipEntry(String name, String content) {
    }
}
