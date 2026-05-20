package com.apkcompare.lang.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class CompareReport {

    private final boolean identical;
    private final String apk1;
    private final String apk2;
    private final LocaleDiff localeDiff;
    private final Map<String, LocaleCompareResult> byLocale;
    private final List<String> warnings;
    private final int apk1StringCount;
    private final int apk2StringCount;

    public CompareReport(
            boolean identical,
            String apk1,
            String apk2,
            LocaleDiff localeDiff,
            Map<String, LocaleCompareResult> byLocale,
            List<String> warnings,
            int apk1StringCount,
            int apk2StringCount) {
        this.identical = identical;
        this.apk1 = apk1;
        this.apk2 = apk2;
        this.localeDiff = localeDiff;
        this.byLocale = Collections.unmodifiableMap(new TreeMap<>(byLocale));
        this.warnings = List.copyOf(warnings);
        this.apk1StringCount = apk1StringCount;
        this.apk2StringCount = apk2StringCount;
    }

    public boolean identical() {
        return identical;
    }

    public String apk1() {
        return apk1;
    }

    public String apk2() {
        return apk2;
    }

    public LocaleDiff localeDiff() {
        return localeDiff;
    }

    public Map<String, LocaleCompareResult> byLocale() {
        return byLocale;
    }

    public List<String> warnings() {
        return warnings;
    }

    /** Total <string> entries summed across all locale buckets. */
    public int apk1StringCount() {
        return apk1StringCount;
    }

    public int apk2StringCount() {
        return apk2StringCount;
    }
}
