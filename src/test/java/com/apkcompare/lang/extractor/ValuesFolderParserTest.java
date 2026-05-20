package com.apkcompare.lang.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValuesFolderParserTest {

    @Test
    void defaultValues() {
        assertEquals("default", ValuesFolderParser.toLocaleTag("values"));
    }

    @Test
    void simpleLanguage() {
        assertEquals("en", ValuesFolderParser.toLocaleTag("values-en"));
    }

    @Test
    void languageAndRegion() {
        assertEquals("zh-CN", ValuesFolderParser.toLocaleTag("values-zh-rCN"));
        assertEquals("zh-TW", ValuesFolderParser.toLocaleTag("values-zh-rTW"));
    }

    @Test
    void nonLocaleQualifier() {
        assertEquals("night", ValuesFolderParser.toLocaleTag("values-night"));
    }

    @Test
    void isValuesFolder() {
        assertTrue(ValuesFolderParser.isValuesFolder("values"));
        assertTrue(ValuesFolderParser.isValuesFolder("values-zh-rCN"));
    }

    @Test
    void extractLocaleQualifier() {
        assertEquals("zh-rCN", ValuesFolderParser.extractLocaleQualifier("zh-rCN-port"));
        assertEquals(null, ValuesFolderParser.extractLocaleQualifier("night"));
    }
}
