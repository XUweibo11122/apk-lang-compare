package com.apkcompare.lang.compare;

import com.apkcompare.lang.model.CompareReport;
import com.apkcompare.lang.model.ExtractionResult;
import com.apkcompare.lang.model.LocaleCompareResult;
import com.apkcompare.lang.model.LocaleDiff;
import com.apkcompare.lang.model.ValueMismatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class ResourceComparator {

    public CompareReport compare(
            String apk1Path,
            String apk2Path,
            ExtractionResult extraction1,
            ExtractionResult extraction2) {
        Map<String, Map<String, String>> map1 = extraction1.localeToStrings();
        Map<String, Map<String, String>> map2 = extraction2.localeToStrings();

        Set<String> locales1 = map1.keySet();
        Set<String> locales2 = map2.keySet();

        List<String> onlyLocalesApk1 = new ArrayList<>();
        List<String> onlyLocalesApk2 = new ArrayList<>();
        for (String locale : locales1) {
            if (!locales2.contains(locale)) {
                onlyLocalesApk1.add(locale);
            }
        }
        for (String locale : locales2) {
            if (!locales1.contains(locale)) {
                onlyLocalesApk2.add(locale);
            }
        }

        LocaleDiff localeDiff = new LocaleDiff(onlyLocalesApk1, onlyLocalesApk2);

        Set<String> allLocales = new TreeSet<>();
        allLocales.addAll(locales1);
        allLocales.addAll(locales2);

        Map<String, LocaleCompareResult> byLocale = new TreeMap<>();
        for (String locale : allLocales) {
            Map<String, String> strings1 = map1.getOrDefault(locale, Map.of());
            Map<String, String> strings2 = map2.getOrDefault(locale, Map.of());
            LocaleCompareResult localeResult = compareLocale(strings1, strings2);
            if (localeResult.hasDifferences()) {
                byLocale.put(locale, localeResult);
            }
        }

        List<String> warnings = new ArrayList<>();
        warnings.addAll(extraction1.warnings());
        warnings.addAll(extraction2.warnings());

        int apk1Count = countStrings(map1);
        int apk2Count = countStrings(map2);
        if (apk1Count == 0) {
            warnings.add("APK1: extracted 0 <string> entries — resources may not have been decoded");
        }
        if (apk2Count == 0) {
            warnings.add("APK2: extracted 0 <string> entries — resources may not have been decoded");
        }

        boolean identical = !localeDiff.hasDifferences()
                && byLocale.isEmpty()
                && warnings.isEmpty();

        return new CompareReport(
                identical, apk1Path, apk2Path, localeDiff, byLocale, warnings, apk1Count, apk2Count);
    }

    static int countStrings(Map<String, Map<String, String>> localeToStrings) {
        int total = 0;
        for (Map<String, String> strings : localeToStrings.values()) {
            total += strings.size();
        }
        return total;
    }

    static LocaleCompareResult compareLocale(Map<String, String> apk1, Map<String, String> apk2) {
        Set<String> keys1 = apk1.keySet();
        Set<String> keys2 = apk2.keySet();

        List<String> onlyInApk1 = new ArrayList<>();
        List<String> onlyInApk2 = new ArrayList<>();
        List<ValueMismatch> mismatches = new ArrayList<>();

        for (String key : keys1) {
            if (!keys2.contains(key)) {
                onlyInApk1.add(key);
            }
        }
        for (String key : keys2) {
            if (!keys1.contains(key)) {
                onlyInApk2.add(key);
            }
        }

        for (String key : keys1) {
            if (!keys2.contains(key)) {
                continue;
            }
            String v1 = apk1.get(key);
            String v2 = apk2.get(key);
            if (!Objects.equals(v1, v2)) {
                mismatches.add(new ValueMismatch(key, v1, v2));
            }
        }

        onlyInApk1.sort(String::compareTo);
        onlyInApk2.sort(String::compareTo);
        mismatches.sort((a, b) -> a.key().compareTo(b.key()));

        return LocaleCompareResult.builder()
                .onlyInApk1(onlyInApk1)
                .onlyInApk2(onlyInApk2)
                .valueMismatch(mismatches)
                .build();
    }
}
