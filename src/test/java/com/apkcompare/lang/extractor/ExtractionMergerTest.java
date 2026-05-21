package com.apkcompare.lang.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.apkcompare.lang.model.ExtractionResult;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExtractionMergerTest {

    @Test
    void mergeBaseAndLangsLocales() {
        ExtractionResult base = ExtractionResult.builder()
                .putLocale("default", Map.of("app_name", "Base"))
                .build();
        ExtractionResult langs = ExtractionResult.builder()
                .putLocale("zh-CN", Map.of("app_name", "中文"))
                .build();

        ExtractionResult merged = ExtractionMerger.merge(base, langs);
        assertEquals("Base", merged.localeToStrings().get("default").get("app_name"));
        assertEquals("中文", merged.localeToStrings().get("zh-CN").get("app_name"));
    }

    @Test
    void mergeOverlappingKeyPrefersLangsWithWarning() {
        ExtractionResult base = ExtractionResult.builder()
                .putLocale("en", Map.of("title", "A"))
                .build();
        ExtractionResult langs = ExtractionResult.builder()
                .putLocale("en", Map.of("title", "B"))
                .build();

        ExtractionResult merged = ExtractionMerger.merge(base, langs);
        assertEquals("B", merged.localeToStrings().get("en").get("title"));
        assertFalse(merged.warnings().isEmpty());
    }
}
