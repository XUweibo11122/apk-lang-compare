package com.apkcompare.lang.report;

import com.apkcompare.lang.compare.ResourceComparator;
import com.apkcompare.lang.model.ExtractionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Path;
import java.util.Map;

/** Writes {@code Map<locale, Map<key, value>>} to JSON. */
public final class ExtractionMapWriter {

    private final ObjectMapper mapper;

    public ExtractionMapWriter() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void write(
            String apkPath,
            Path decodeDir,
            ExtractionResult extraction,
            Path outputPath) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        root.put("apk", apkPath);
        root.put("decodeDir", decodeDir.toAbsolutePath().toString());
        root.put("stringCount", ResourceComparator.countStrings(extraction.localeToStrings()));

        ObjectNode locales = root.putObject("locales");
        for (Map.Entry<String, Map<String, String>> entry : extraction.localeToStrings().entrySet()) {
            ObjectNode keys = locales.putObject(entry.getKey());
            for (Map.Entry<String, String> stringEntry : entry.getValue().entrySet()) {
                keys.put(stringEntry.getKey(), stringEntry.getValue());
            }
        }

        ArrayNode warnings = root.putArray("warnings");
        for (String warning : extraction.warnings()) {
            warnings.add(warning);
        }

        mapper.writeValue(outputPath.toFile(), root);
    }
}
