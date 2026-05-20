package com.apkcompare.lang.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.apkcompare.lang.model.ExtractionResult;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApktoolResourceExtractorTest {

    @Test
    void decodeCommandUsesNoSrcNotNoRes() {
        List<String> cmd = ApktoolResourceExtractor.buildDecodeCommand(
                Path.of("tools/apktool.jar"), Path.of("a.apk"), Path.of("out"));
        assertTrue(cmd.contains("-s"), "must pass -s (--no-src) to decode resources without smali");
        assertFalse(cmd.contains("-r"), "must not pass -r (--no-res) which skips resource decode");
    }

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
