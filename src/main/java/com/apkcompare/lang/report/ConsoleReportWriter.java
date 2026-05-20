package com.apkcompare.lang.report;

import com.apkcompare.lang.model.CompareReport;
import com.apkcompare.lang.model.LocaleCompareResult;
import com.apkcompare.lang.model.ValueMismatch;
import java.io.PrintStream;
import java.util.Map;

public final class ConsoleReportWriter {

    private final boolean quiet;

    public ConsoleReportWriter(boolean quiet) {
        this.quiet = quiet;
    }

    public void write(CompareReport report, PrintStream out) {
        out.println("Result: " + (report.identical() ? "IDENTICAL" : "DIFFERENT"));
        if (quiet) {
            if (!report.identical()) {
                summarizeCounts(report, out);
            }
            return;
        }

        out.println("APK1: " + report.apk1());
        out.println("APK2: " + report.apk2());
        out.printf(
                "Extracted strings: APK1=%d, APK2=%d%n",
                report.apk1StringCount(), report.apk2StringCount());

        if (!report.localeDiff().onlyInApk1().isEmpty()) {
            out.println("Locales only in APK1: " + report.localeDiff().onlyInApk1());
        }
        if (!report.localeDiff().onlyInApk2().isEmpty()) {
            out.println("Locales only in APK2: " + report.localeDiff().onlyInApk2());
        }

        for (Map.Entry<String, LocaleCompareResult> entry : report.byLocale().entrySet()) {
            writeLocaleDetail(entry.getKey(), entry.getValue(), out);
        }

        if (!report.warnings().isEmpty()) {
            out.println("Warnings:");
            for (String warning : report.warnings()) {
                out.println("  - " + warning);
            }
        }
    }

    private static void summarizeCounts(CompareReport report, PrintStream out) {
        int localeOnly1 = report.localeDiff().onlyInApk1().size();
        int localeOnly2 = report.localeDiff().onlyInApk2().size();
        int localesWithDiff = report.byLocale().size();
        out.printf(
                "Summary: %d locales only in APK1, %d only in APK2, %d locales with key/value diffs%n",
                localeOnly1, localeOnly2, localesWithDiff);
    }

    private static void writeLocaleDetail(String locale, LocaleCompareResult result, PrintStream out) {
        int keyOnly1 = result.onlyInApk1().size();
        int keyOnly2 = result.onlyInApk2().size();
        int mismatches = result.valueMismatch().size();
        out.printf(
                "Locale %s: %d keys only in APK1, %d keys only in APK2, %d value mismatch(es)%n",
                locale, keyOnly1, keyOnly2, mismatches);

        for (String key : result.onlyInApk1()) {
            out.printf("  onlyInApk1: %s%n", key);
        }
        for (String key : result.onlyInApk2()) {
            out.printf("  onlyInApk2: %s%n", key);
        }
        for (ValueMismatch mismatch : result.valueMismatch()) {
            out.printf(
                    "  valueMismatch: %s  \"%s\" vs \"%s\"%n",
                    mismatch.key(), escape(mismatch.apk1()), escape(mismatch.apk2()));
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\n", "\\n").replace("\r", "\\r");
    }
}
