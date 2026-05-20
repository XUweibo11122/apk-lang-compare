package com.apkcompare.lang.report;

import com.apkcompare.lang.model.CompareReport;
import com.apkcompare.lang.model.LocaleCompareResult;
import com.apkcompare.lang.model.ValueMismatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Path;

public final class JsonReportWriter {

    private final ObjectMapper mapper;

    public JsonReportWriter() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void write(CompareReport report, Path outputPath) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        root.put("identical", report.identical());
        root.put("apk1", report.apk1());
        root.put("apk2", report.apk2());
        ObjectNode stats = root.putObject("stats");
        stats.put("apk1StringCount", report.apk1StringCount());
        stats.put("apk2StringCount", report.apk2StringCount());

        ObjectNode localeDiff = root.putObject("localeDiff");
        localeDiff.set("onlyInApk1", toArray(report.localeDiff().onlyInApk1()));
        localeDiff.set("onlyInApk2", toArray(report.localeDiff().onlyInApk2()));

        ObjectNode byLocale = root.putObject("byLocale");
        for (var entry : report.byLocale().entrySet()) {
            byLocale.set(entry.getKey(), toLocaleNode(entry.getValue()));
        }

        ArrayNode warnings = root.putArray("warnings");
        for (String warning : report.warnings()) {
            warnings.add(warning);
        }

        mapper.writeValue(outputPath.toFile(), root);
    }

    private ObjectNode toLocaleNode(LocaleCompareResult result) {
        ObjectNode node = mapper.createObjectNode();
        node.set("onlyInApk1", toArray(result.onlyInApk1()));
        node.set("onlyInApk2", toArray(result.onlyInApk2()));
        ArrayNode mismatches = node.putArray("valueMismatch");
        for (ValueMismatch mismatch : result.valueMismatch()) {
            ObjectNode item = mismatches.addObject();
            item.put("key", mismatch.key());
            item.put("apk1", mismatch.apk1());
            item.put("apk2", mismatch.apk2());
        }
        return node;
    }

    private ArrayNode toArray(Iterable<String> values) {
        ArrayNode array = mapper.createArrayNode();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }
}
