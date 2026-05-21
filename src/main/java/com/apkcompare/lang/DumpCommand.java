package com.apkcompare.lang;

import com.apkcompare.lang.compare.ResourceComparator;
import com.apkcompare.lang.extractor.ApktoolResourceExtractor;
import com.apkcompare.lang.model.ExtractionResult;
import com.apkcompare.lang.report.ExtractionMapWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "dump",
        description = "Decode one APK with apktool, keep decoded files, and export locale→key→value map as JSON.")
public class DumpCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "APK file path")
    private Path apk;

    @Option(
            names = {"-o", "--output"},
            required = true,
            description = "Write Map<locale, Map<key, value>> JSON to this file")
    private Path output;

    @Option(
            names = {"-d", "--decode-dir"},
            description = "Directory to save apktool decoded output (default: <apk-name>-decoded beside APK)")
    private Path decodeDir;

    @Option(names = "--apktool", description = "Path to apktool.jar or apktool executable")
    private Path apktool;

    @Override
    public Integer call() throws Exception {
        ApkValidation.validateApk(apk, "apk");

        Path apktoolPath = ApktoolResourceExtractor.resolveApktool(apktool);
        Path decodeOutput = resolveDecodeDir(apk, decodeDir);

        ApktoolResourceExtractor extractor = new ApktoolResourceExtractor(apktoolPath, true);
        ExtractionResult extraction = extractor.extractToDirectory(apk, decodeOutput);

        new ExtractionMapWriter()
                .write(apk.toAbsolutePath().toString(), decodeOutput, extraction, output);

        int count = ResourceComparator.countStrings(extraction.localeToStrings());
        System.out.println("Decoded APK saved to: " + decodeOutput.toAbsolutePath());
        System.out.println("String map written to: " + output.toAbsolutePath());
        System.out.printf("Locales: %d, total <string> entries: %d%n", extraction.localeToStrings().size(), count);
        if (!extraction.warnings().isEmpty()) {
            System.out.printf("Warnings: %d (see JSON)%n", extraction.warnings().size());
        }

        return ApkLangCli.EXIT_OK;
    }

    static Path resolveDecodeDir(Path apk, Path userDir) throws Exception {
        if (userDir != null) {
            Files.createDirectories(userDir);
            return userDir.toAbsolutePath();
        }
        String baseName = apk.getFileName().toString();
        if (baseName.toLowerCase().endsWith(".apk")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }
        Path parent = apk.getParent() != null ? apk.getParent() : Path.of(".");
        Path dir = parent.resolve(baseName + "-decoded");
        Files.createDirectories(dir);
        return dir.toAbsolutePath();
    }
}
