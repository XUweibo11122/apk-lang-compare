package com.apkcompare.lang;

import java.nio.file.Files;
import java.nio.file.Path;

final class ApkValidation {

    private ApkValidation() {}

    static void validateApk(Path path, String label) {
        if (path == null) {
            throw new IllegalArgumentException("Missing " + label + " path");
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Not a file (" + label + "): " + path);
        }
        if (!path.getFileName().toString().toLowerCase().endsWith(".apk")) {
            throw new IllegalArgumentException("Expected .apk file (" + label + "): " + path);
        }
    }
}
