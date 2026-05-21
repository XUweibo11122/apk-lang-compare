package com.apkcompare.lang.extractor;

import com.apkcompare.lang.model.ExtractionResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads {@code <string>} entries from an LPK treated as a ZIP (typical when converted from a resource-only APK).
 */
final class LpkZipStringScanner {

    private LpkZipStringScanner() {}

    static ExtractionResult scan(byte[] lpkBytes, String lpkName, String localeHint) throws IOException {
        ExtractionResult.Builder result = ExtractionResult.builder();
        Map<String, Map<String, String>> localeToStrings = new TreeMap<>();
        boolean foundValues = false;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(lpkBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName().replace('\\', '/');
                if (!name.endsWith(".xml")) {
                    continue;
                }
                String valuesDir = findValuesDirName(name);
                if (valuesDir == null) {
                    continue;
                }
                foundValues = true;
                String localeTag = ValuesFolderParser.toLocaleTag(valuesDir);
                if ("default".equals(localeTag) && localeHint != null && !localeHint.isBlank()) {
                    localeTag = localeHint;
                }
                Map<String, String> merged =
                        localeToStrings.computeIfAbsent(localeTag, k -> new TreeMap<>());
                byte[] xmlBytes = readZipEntryBytes(zis, entry);
                parseEntryXml(xmlBytes, name, valuesDir, localeTag, merged, result);
            }
        }

        if (!foundValues && localeHint != null && !localeHint.isBlank()) {
            result.addWarning(String.format(
                    "LPK %s: no res/values* XML found in zip; cannot extract strings", lpkName));
        }
        for (Map.Entry<String, Map<String, String>> e : localeToStrings.entrySet()) {
            result.putLocale(e.getKey(), e.getValue());
        }
        return result.build();
    }

    private static String findValuesDirName(String entryPath) {
        String[] parts = entryPath.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (ValuesFolderParser.isValuesFolder(parts[i])) {
                return parts[i];
            }
        }
        return null;
    }

    private static byte[] readZipEntryBytes(ZipInputStream zis, ZipEntry entry) throws IOException {
        long size = entry.getSize();
        if (size > 0 && size < Integer.MAX_VALUE) {
            return zis.readNBytes((int) size);
        }
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = zis.read(buffer)) != -1) {
            bos.write(buffer, 0, read);
        }
        return bos.toByteArray();
    }

    private static void parseEntryXml(
            byte[] xmlBytes,
            String entryName,
            String valuesDirName,
            String localeTag,
            Map<String, String> merged,
            ExtractionResult.Builder result)
            throws IOException {
        try (InputStream in = new ByteArrayInputStream(xmlBytes)) {
            XmlStringParser.ParseResult parsed = XmlStringParser.parse(in);
            for (String dupKey : parsed.duplicateKeys()) {
                result.addWarning(String.format(
                        "Duplicate key '%s' in %s (%s), later value kept",
                        dupKey, entryName, valuesDirName));
            }
            for (Map.Entry<String, String> entry : parsed.strings().entrySet()) {
                if (merged.containsKey(entry.getKey())) {
                    result.addWarning(String.format(
                            "Duplicate key '%s' in locale '%s' (LPK %s), later value kept",
                            entry.getKey(), localeTag, entryName));
                }
                merged.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse " + entryName + ": " + e.getMessage(), e);
        }
    }
}
