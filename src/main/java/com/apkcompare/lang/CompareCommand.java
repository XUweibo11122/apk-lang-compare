package com.apkcompare.lang;

import com.apkcompare.lang.compare.ResourceComparator;
import com.apkcompare.lang.extractor.ApktoolResourceExtractor;
import com.apkcompare.lang.model.CompareReport;
import com.apkcompare.lang.model.ExtractionResult;
import com.apkcompare.lang.report.ConsoleReportWriter;
import com.apkcompare.lang.report.JsonReportWriter;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "compare", description = "Compare string resources between two APK files.")
public class CompareCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "First APK file path")
    private Path apk1;

    @Parameters(index = "1", description = "Second APK file path")
    private Path apk2;

    @Option(names = {"-o", "--output"}, description = "Write JSON compare report to this file")
    private Path output;

    @Option(names = {"-q", "--quiet"}, description = "Print summary only")
    private boolean quiet;

    @Option(names = "--apktool", description = "Path to apktool.jar or apktool executable")
    private Path apktool;

    @Option(names = "--keep-temp", description = "Keep apktool decode temp directories (compare mode)")
    private boolean keepTemp;

    @Override
    public Integer call() throws Exception {
        ApkValidation.validateApk(apk1, "apk1");
        ApkValidation.validateApk(apk2, "apk2");

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

        return report.identical() ? ApkLangCli.EXIT_IDENTICAL : ApkLangCli.EXIT_DIFFERENT;
    }
}
