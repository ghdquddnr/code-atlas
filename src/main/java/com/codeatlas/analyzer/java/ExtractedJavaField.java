package com.codeatlas.analyzer.java;

public record ExtractedJavaField(
        String name,
        String type
) {

    public String toSummary() {
        return name + ":" + type;
    }
}
