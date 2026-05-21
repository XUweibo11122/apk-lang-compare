package com.apkcompare.lang.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.apkcompare.lang.compare.ResourceComparator;
import com.apkcompare.lang.model.ExtractionResult;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LangsBrExtractorTest {

    @Test
    void isLangsBrEntry() {
        assertTrue(LangsBrExtractor.isLangsBrEntry("assets/langs/pack.br"));
        assertTrue(LangsBrExtractor.isLangsBrEntry("assets/langs/xxxxxx.br"));
        assertTrue(LangsBrExtractor.isLangsBrEntry("langs/pack.br"));
        assertFalse(LangsBrExtractor.isLangsBrEntry("assets/lang/pack.br"));
        assertFalse(LangsBrExtractor.isLangsBrEntry("res/langs/pack.br"));
    }

    @Test
    void extractStringsFromBrLpkArchive(@TempDir Path temp) throws Exception {
        Path apk = LangsBrTestFixtures.createApkWithLangsBr(temp);
        ExtractionResult result = new LangsBrExtractor(Path.of("tools/apktool.jar")).extract(apk);

        assertTrue(result.localeToStrings().containsKey("en"));
        assertTrue(result.localeToStrings().containsKey("zh-CN"));
        assertEquals("Hello", result.localeToStrings().get("en").get("app_name"));
        assertEquals("你好", result.localeToStrings().get("zh-CN").get("app_name"));
        assertEquals("EN", result.localeToStrings().get("en").get("only_en"));
        assertEquals(4, ResourceComparator.countStrings(result.localeToStrings()));
    }

    @Test
    void lpkFilenameLocaleTag() {
        assertEquals("zh-CN", LpkFilenameParser.toLocaleTag("zh-rCN.lpk"));
        assertEquals("en", LpkFilenameParser.toLocaleTag("en.lpk"));
        assertEquals("vi", LpkFilenameParser.toLocaleTag("base-vi.lpk"));
        assertEquals("zu", LpkFilenameParser.toLocaleTag("base-zu.lpk"));
    }

    @Test
    void applyLocaleHintMapsDefaultBucket() {
        ExtractionResult decoded = ExtractionResult.builder()
                .putLocale("default", java.util.Map.of("key1", "value1"))
                .build();
        ExtractionResult mapped = LangsBrExtractor.applyLocaleHint(decoded, "vi", "base-vi.lpk");
        assertEquals("value1", mapped.localeToStrings().get("vi").get("key1"));
        assertFalse(mapped.localeToStrings().containsKey("default"));
    }
}
