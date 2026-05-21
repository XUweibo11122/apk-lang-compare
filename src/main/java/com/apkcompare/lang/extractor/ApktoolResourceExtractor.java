package com.apkcompare.lang.extractor;

import com.apkcompare.lang.model.ExtractionResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public final class ApktoolResourceExtractor implements StringResourceExtractor {

    private final Path apktoolPath;
    private final boolean keepTemp;

    public ApktoolResourceExtractor(Path apktoolPath, boolean keepTemp) {
        this.apktoolPath = apktoolPath;
        this.keepTemp = keepTemp;
    }

    @Override
    public ExtractionResult extract(Path apkPath) throws Exception {
        Path tempDir = Files.createTempDirectory("apk-lang-");
        try {
            return extractToDirectory(apkPath, tempDir);
        } finally {
            if (!keepTemp) {
                deleteRecursive(tempDir);
            }
        }
    }

    /**
     * Decodes APK into {@code decodeDir} (apktool {@code -o}) and scans {@code res/values*} strings.
     * The decoded directory is kept on disk.
     */
    public ExtractionResult extractToDirectory(Path apkPath, Path decodeDir) throws Exception {
        Files.createDirectories(decodeDir);
        runApktool(apkPath, decodeDir);
        return scanDecodedResources(decodeDir);
    }

    public ExtractionResult scanDecodedResources(Path decodedRoot) throws IOException {
        Path resDir = decodedRoot.resolve("res");
        if (!Files.isDirectory(resDir)) {
            throw new IOException("Decoded APK has no res/ directory: " + decodedRoot);
        }

        ExtractionResult.Builder result = ExtractionResult.builder();
        Map<String, Map<String, String>> localeToStrings = new TreeMap<>();

        try (Stream<Path> valuesDirs = Files.list(resDir)) {
            List<Path> dirs = valuesDirs
                    .filter(Files::isDirectory)
                    .filter(p -> ValuesFolderParser.isValuesFolder(p.getFileName().toString()))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();

            for (Path valuesDir : dirs) {
                String dirName = valuesDir.getFileName().toString();
                String localeTag = ValuesFolderParser.toLocaleTag(dirName);
                Map<String, String> merged = localeToStrings.computeIfAbsent(localeTag, k -> new TreeMap<>());

                try (Stream<Path> xmlFiles = Files.list(valuesDir)) {
                    List<Path> xmls = xmlFiles
                            .filter(p -> p.getFileName().toString().endsWith(".xml"))
                            .sorted()
                            .toList();
                    for (Path xml : xmls) {
                        parseXmlFile(xml, dirName, localeTag, merged, result);
                    }
                }
            }
        }

        for (Map.Entry<String, Map<String, String>> entry : localeToStrings.entrySet()) {
            result.putLocale(entry.getKey(), entry.getValue());
        }
        return result.build();
    }

    private void parseXmlFile(
            Path xml,
            String valuesDirName,
            String localeTag,
            Map<String, String> merged,
            ExtractionResult.Builder result) throws IOException {
        try (InputStream in = Files.newInputStream(xml)) {
            XmlStringParser.ParseResult parsed = XmlStringParser.parse(in);
            for (String dupKey : parsed.duplicateKeys()) {
                result.addWarning(String.format(
                        "Duplicate key '%s' in %s (%s), later value kept",
                        dupKey, xml.getFileName(), valuesDirName));
            }
            for (Map.Entry<String, String> entry : parsed.strings().entrySet()) {
                if (merged.containsKey(entry.getKey())) {
                    result.addWarning(String.format(
                            "Duplicate key '%s' across XML files in locale '%s' (%s), later value kept",
                            entry.getKey(), localeTag, valuesDirName));
                }
                merged.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse " + xml + ": " + e.getMessage(), e);
        }
    }

    private void runApktool(Path apkPath, Path outputDir) throws IOException, InterruptedException {
        List<String> command = buildDecodeCommand(apktoolPath, apkPath, outputDir);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("apktool failed (exit " + exitCode + "): " + output.trim());
        }
    }

    static List<String> buildDecodeCommand(Path apktoolPath, Path apkPath, Path outputDir) {
        List<String> command = new ArrayList<>();
        String fileName = apktoolPath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".jar")) {
            command.add("java");
            command.add("-jar");
            command.add(apktoolPath.toAbsolutePath().toString());
        } else {
            command.add(apktoolPath.toAbsolutePath().toString());
        }
        command.add("d");
        command.add("-f");
        // -s = --no-src: decode resources only, skip smali/dex (-r would mean --no-res!)
        command.add("-s");
        command.add(apkPath.toAbsolutePath().toString());
        command.add("-o");
        command.add(outputDir.toAbsolutePath().toString());
        return command;
    }

    private static void deleteRecursive(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, new java.nio.file.SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static Path resolveApktool(Path userPath) throws IOException {
        if (userPath != null) {
            if (!Files.exists(userPath)) {
                throw new IOException("apktool not found at: " + userPath);
            }
            return userPath;
        }
        String pathApktool = findOnPath("apktool");
        if (pathApktool != null) {
            return Path.of(pathApktool);
        }
        String pathApktoolBat = findOnPath("apktool.bat");
        if (pathApktoolBat != null) {
            return Path.of(pathApktoolBat);
        }
        throw new IOException(
                "apktool not found in PATH. Install from https://apktool.org/ or use --apktool <path>");
    }

    private static String findOnPath(String name) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isBlank()) {
            return null;
        }
        String separator = System.getProperty("path.separator");
        for (String dir : pathEnv.split(separator)) {
            Path candidate = Path.of(dir, name);
            if (Files.isRegularFile(candidate)) {
                return candidate.toString();
            }
        }
        return null;
    }
}
