package com.apkcompare.lang.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.apkcompare.lang.model.ExtractionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExtractionMapWriterTest {

    @Test
    void writesLocaleKeyValueMap(@TempDir Path temp) throws Exception {
        ExtractionResult extraction = ExtractionResult.builder()
                .putLocale("default", java.util.Map.of("app_name", "My App"))
                .putLocale("zh-CN", java.util.Map.of("app_name", "我的应用"))
                .build();

        Path json = temp.resolve("strings.json");
        new ExtractionMapWriter()
                .write("/test/app.apk", temp.resolve("decoded"), extraction, json);

        JsonNode root = new ObjectMapper().readTree(json.toFile());
        assertEquals("/test/app.apk", root.get("apk").asText());
        assertEquals(2, root.get("stringCount").asInt());
        assertEquals("My App", root.get("locales").get("default").get("app_name").asText());
        assertEquals("我的应用", root.get("locales").get("zh-CN").get("app_name").asText());
        assertTrue(Files.isRegularFile(json));
    }
}
