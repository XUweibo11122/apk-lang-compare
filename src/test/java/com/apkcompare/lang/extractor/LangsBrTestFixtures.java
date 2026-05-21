package com.apkcompare.lang.extractor;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class LangsBrTestFixtures {

    private LangsBrTestFixtures() {}

    static Path createApkWithLangsBr(Path dir) throws IOException {
        byte[] enLpk = lpkZip("res/values-en/strings.xml", enStringsXml());
        byte[] zhLpk = lpkZip("res/values-zh-rCN/strings.xml", zhStringsXml());
        byte[] innerZip = zipBytes(Map.of("en.lpk", enLpk, "zh-rCN.lpk", zhLpk));
        byte[] br = brotli(innerZip);

        Path apk = dir.resolve("with-langs.apk");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(apk))) {
            put(zos, "assets/langs/pack.br", br);
            put(zos, "AndroidManifest.xml", "<manifest package=\"test\"/>".getBytes(StandardCharsets.UTF_8));
        }
        return apk;
    }

    private static byte[] lpkZip(String xmlPath, String xml) throws IOException {
        return zipBytes(Map.of(xmlPath, xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static byte[] zipBytes(Map<String, byte[]> entries) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (var e : entries.entrySet()) {
                put(zos, e.getKey(), e.getValue());
            }
        }
        return bos.toByteArray();
    }

    private static void put(ZipOutputStream zos, String name, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

    private static byte[] brotli(byte[] input) throws IOException {
        Brotli4jLoader.ensureAvailability();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (BrotliOutputStream brotli = new BrotliOutputStream(bos, new Encoder.Parameters())) {
            brotli.write(input);
        }
        return bos.toByteArray();
    }

    private static String enStringsXml() {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="app_name">Hello</string>
                    <string name="only_en">EN</string>
                </resources>
                """;
    }

    private static String zhStringsXml() {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="app_name">你好</string>
                    <string name="only_zh">ZH</string>
                </resources>
                """;
    }
}
