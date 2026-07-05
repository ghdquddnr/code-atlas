package com.codeatlas.analysis.dto;

public record MethodCallStepResponse(
        String sourceClassName,
        String sourceMethodName,
        String targetClassName,
        String targetMethodName
) {

    public static MethodCallStepResponse parse(String line) {
        String[] parts = line.split("->", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid method call step: " + line);
        }
        String[] source = splitMethod(parts[0]);
        String[] target = splitMethod(parts[1]);
        return new MethodCallStepResponse(source[0], source[1], target[0], target[1]);
    }

    private static String[] splitMethod(String value) {
        int separator = value.lastIndexOf('.');
        if (separator < 1 || separator == value.length() - 1) {
            throw new IllegalArgumentException("Invalid method reference: " + value);
        }
        return new String[] { value.substring(0, separator), value.substring(separator + 1) };
    }
}
