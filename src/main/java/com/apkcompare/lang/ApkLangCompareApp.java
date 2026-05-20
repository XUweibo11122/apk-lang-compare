package com.apkcompare.lang;

import com.apkcompare.lang.compare.ResourceComparator;
import com.apkcompare.lang.extractor.ApktoolResourceExtractor;
import com.apkcompare.lang.model.CompareReport;
import com.apkcompare.lang.model.ExtractionResult;
import com.apkcompare.lang.report.ConsoleReportWriter;
import com.apkcompare.lang.report.JsonReportWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "apk-lang-compare",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Compare multi-language string resources between two APK files.")
public class ApkLangCompareApp implements Callable<Integer> {

    public static final int EXIT_IDENTICAL = 0;
    public static final int EXIT_DIFFERENT = 1;
    public static final int EXIT_ERROR = 2;

    @Parameters(index = "0", description = "First APK file path")
    private Path apk1;

    @Parameters(index = "1", description = "Second APK file path")
    private Path apk2;

    @Option(names = {"-o", "--output"}, description = "Write JSON report to this file")
    private Path output;

    @Option(names = {"-q", "--quiet"}, description = "Print summary only")
    private boolean quiet;

    @Option(names = "--apktool", description = "Path to apktool.jar or apktool executable")
    private Path apktool;

    @Option(names = "--keep-temp", description = "Keep apktool decode temp directories")
    private boolean keepTemp;

    public static void main(String[] args) {
        ApkLangCompareApp app = new ApkLangCompareApp();
        CommandLine cmd = new CommandLine(app);
        cmd.setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            System.err.println("Error: " + cause.getMessage());
            return EXIT_ERROR;
        });
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        validateApk(apk1, "apk1");
        validateApk(apk2, "apk2");

        Path apktoolPath = ApktoolResourceExtractor.resolveApktool(apktool);
        ApktoolResourceExtractor extractor = new ApktoolResourceExtractor(apktoolPath, keepTemp);

        ExtractionResult extraction1 = extractor.extract(apk1);
        ExtractionResult extraction2 = extractor.extract(apk2);

        CompareReport report = new ResourceComparator()
                .compare(
                        apk1.toAbsolutePath().toString(),
                        apk2.toAbsolutePath().toString(),
                        extraction1,
                        extraction2);

        new ConsoleReportWriter(quiet).write(report, System.out);

        if (output != null) {
            new JsonReportWriter().write(report, output);
            if (!quiet) {
                System.out.println("JSON report written to: " + output.toAbsolutePath());
            }
        }

        return report.identical() ? EXIT_IDENTICAL : EXIT_DIFFERENT;
    }

    private static void validateApk(Path path, String label) {
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
