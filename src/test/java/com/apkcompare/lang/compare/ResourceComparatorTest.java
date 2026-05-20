package com.apkcompare.lang.compare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.apkcompare.lang.model.CompareReport;
import com.apkcompare.lang.model.ExtractionResult;
import com.apkcompare.lang.model.LocaleCompareResult;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ResourceComparatorTest {

    private final ResourceComparator comparator = new ResourceComparator();

    @Test
    void identicalResources() {
        Map<String, String> en = Map.of("app_name", "MyApp", "hello", "Hello");
        ExtractionResult apk1 = ExtractionResult.builder()
                .putLocale("default", Map.of("app_name", "MyApp"))
                .putLocale("en", en)
                .build();
        ExtractionResult apk2 = ExtractionResult.builder()
                .putLocale("default", Map.of("app_name", "MyApp"))
                .putLocale("en", Map.of("app_name", "MyApp", "hello", "Hello"))
                .build();

        CompareReport report = comparator.compare("/a.apk", "/b.apk", apk1, apk2);
        assertTrue(report.identical());
        assertTrue(report.byLocale().isEmpty());
        assertTrue(report.localeDiff().onlyInApk1().isEmpty());
        assertTrue(report.localeDiff().onlyInApk2().isEmpty());
    }

    @Test
    void localeOnlyInApk1() {
        ExtractionResult apk1 = ExtractionResult.builder()
                .putLocale("ja", Map.of("greeting", "こんにちは"))
                .build();
        ExtractionResult apk2 = ExtractionResult.builder().build();

        CompareReport report = comparator.compare("/a.apk", "/b.apk", apk1, apk2);
        assertFalse(report.identical());
        assertEquals(1, report.localeDiff().onlyInApk1().size());
        assertEquals("ja", report.localeDiff().onlyInApk1().get(0));
        assertTrue(report.byLocale().containsKey("ja"));
        assertEquals(1, report.byLocale().get("ja").onlyInApk1().size());
    }

    @Test
    void keyAndValueDifferences() {
        ExtractionResult apk1 = ExtractionResult.builder()
                .putLocale("zh-CN", Map.of("app_name", "MyApp", "only_a", "A"))
                .build();
        ExtractionResult apk2 = ExtractionResult.builder()
                .putLocale("zh-CN", Map.of("app_name", "我的应用", "only_b", "B"))
                .build();

        CompareReport report = comparator.compare("/a.apk", "/b.apk", apk1, apk2);
        assertFalse(report.identical());
        LocaleCompareResult zh = report.byLocale().get("zh-CN");
        assertEquals(1, zh.onlyInApk1().size());
        assertEquals("only_a", zh.onlyInApk1().get(0));
        assertEquals(1, zh.onlyInApk2().size());
        assertEquals("only_b", zh.onlyInApk2().get(0));
        assertEquals(1, zh.valueMismatch().size());
        assertEquals("app_name", zh.valueMismatch().get(0).key());
        assertEquals("MyApp", zh.valueMismatch().get(0).apk1());
        assertEquals("我的应用", zh.valueMismatch().get(0).apk2());
    }

    @Test
    void warningsMakeNotIdentical() {
        ExtractionResult apk1 = ExtractionResult.builder()
                .putLocale("default", Map.of("x", "1"))
                .addWarning("duplicate key")
                .build();
        ExtractionResult apk2 = ExtractionResult.builder()
                .putLocale("default", Map.of("x", "1"))
                .build();

        CompareReport report = comparator.compare("/a.apk", "/b.apk", apk1, apk2);
        assertFalse(report.identical());
        assertEquals(1, report.warnings().size());
    }

    @Test
    void compareLocaleDirect() {
        LocaleCompareResult result = ResourceComparator.compareLocale(
                Map.of("a", "1", "b", "2"),
                Map.of("a", "1", "c", "3"));
        assertTrue(result.hasDifferences());
        assertEquals(1, result.onlyInApk1().size());
        assertEquals(1, result.onlyInApk2().size());
        assertEquals(1, ResourceComparator.compareLocale(
                Map.of("a", "x"), Map.of("a", "y")).valueMismatch().size());
    }
}
