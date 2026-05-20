package com.apkcompare.lang.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.apkcompare.lang.model.ExtractionResult;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ApktoolResourceExtractorTest {

    @Test
    void scanDecodedResourcesFromFixture() throws Exception {
        Path fixture = Path.of("src/test/resources/decoded-res");
        ApktoolResourceExtractor extractor = new ApktoolResourceExtractor(Path.of("apktool"), true);
        ExtractionResult result = extractor.scanDecodedResources(fixture);

        assertTrue(result.localeToStrings().containsKey("default"));
        assertTrue(result.localeToStrings().containsKey("zh-CN"));
        assertTrue(result.localeToStrings().containsKey("en"));

        assertEquals("My Application", result.localeToStrings().get("default").get("app_name"));
        assertEquals("我的应用", result.localeToStrings().get("zh-CN").get("app_name"));
        assertEquals("My Application", result.localeToStrings().get("en").get("app_name"));
        assertEquals("Hello", result.localeToStrings().get("en").get("hello"));
    }

    @Test
    void duplicateKeyProducesWarning() throws Exception {
        Path fixture = Path.of("src/test/resources/decoded-res-dup");
        ApktoolResourceExtractor extractor = new ApktoolResourceExtractor(Path.of("apktool"), true);
        ExtractionResult result = extractor.scanDecodedResources(fixture);

        assertFalse(result.warnings().isEmpty());
        assertEquals("second", result.localeToStrings().get("default").get("dup_key"));
    }
}
