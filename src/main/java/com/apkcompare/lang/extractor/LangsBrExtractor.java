package com.apkcompare.lang.extractor;

import com.apkcompare.lang.model.ExtractionResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Extracts strings from {@code assets/langs/*.br} inside an APK: Brotli → (usually ZIP) → {@code *.lpk} per language.
 */
public final class LangsBrExtractor {

    private static final String ASSETS_LANGS_PREFIX = "assets/langs/";
    private static final String LEGACY_LANGS_PREFIX = "langs/";

    private final Path apktoolPath;

    public LangsBrExtractor(Path apktoolPath) {
        this.apktoolPath = apktoolPath;
    }

    public ExtractionResult extract(Path apkPath) throws Exception {
        ExtractionResult.Builder merged = ExtractionResult.builder();
        Map<String, Map<String, String>> localeToStrings = new TreeMap<>();

        try (ZipFile apkZip = new ZipFile(apkPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = apkZip.entries();
            boolean foundBr = false;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName().replace('\\', '/');
                if (!isLangsBrEntry(name)) {
                    continue;
                }
                foundBr = true;
                byte[] brBytes;
                try (InputStream in = apkZip.getInputStream(entry)) {
                    brBytes = in.readAllBytes();
                }
                mergeFromBrArchive(name, brBytes, localeToStrings, merged);
            }
            if (!foundBr) {
                merged.addWarning("No assets/langs/*.br found in APK (skipped langs pack comparison)");
            }
        }

        for (Map.Entry<String, Map<String, String>> e : localeToStrings.entrySet()) {
            merged.putLocale(e.getKey(), e.getValue());
        }
        return merged.build();
    }

    private void mergeFromBrArchive(
            String brEntryPath,
            byte[] brBytes,
            Map<String, Map<String, String>> localeToStrings,
            ExtractionResult.Builder warningsOut)
            throws Exception {
        byte[] decompressed = BrotliUtil.decompress(brBytes);
        if (isZipMagic(decompressed)) {
            parseZipContainer(brEntryPath, decompressed, localeToStrings, warningsOut);
            return;
        }
        if (looksLikeLpkPath(brEntryPath)) {
            String locale = LpkFilenameParser.toLocaleTag(Path.of(brEntryPath).getFileName().toString());
            mergeLpk(brEntryPath, decompressed, locale, localeToStrings, warningsOut);
            return;
        }
        warningsOut.addWarning("Unsupported langs archive (not zip after brotli): " + brEntryPath);
    }

    private void parseZipContainer(
            String brEntryPath,
            byte[] zipBytes,
            Map<String, Map<String, String>> localeToStrings,
            ExtractionResult.Builder warningsOut)
            throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName().replace('\\', '/');
                if (!name.toLowerCase().endsWith(".lpk")) {
                    continue;
                }
                String fileName = Path.of(name).getFileName().toString();
                String localeHint = LpkFilenameParser.toLocaleTag(fileName);
                byte[] lpkBytes = readZipEntryBytes(zis, entry);
                mergeLpk(brEntryPath + "!" + name, lpkBytes, localeHint, localeToStrings, warningsOut);
            }
        }
    }

    private void mergeLpk(
            String sourceLabel,
            byte[] lpkBytes,
            String localeHint,
            Map<String, Map<String, String>> localeToStrings,
            ExtractionResult.Builder warningsOut)
            throws Exception {
        ExtractionResult fromLpk;
        if (isZipMagic(lpkBytes)) {
            fromLpk = LpkZipStringScanner.scan(lpkBytes, sourceLabel, localeHint);
        } else {
            fromLpk = decodeLpkWithApktool(lpkBytes, sourceLabel, localeHint);
        }
        for (String warning : fromLpk.warnings()) {
            warningsOut.addWarning(warning);
        }
        for (Map.Entry<String, Map<String, String>> localeEntry : fromLpk.localeToStrings().entrySet()) {
            String locale = localeEntry.getKey();
            Map<String, String> target = localeToStrings.computeIfAbsent(locale, k -> new TreeMap<>());
            for (Map.Entry<String, String> stringEntry : localeEntry.getValue().entrySet()) {
                if (target.containsKey(stringEntry.getKey())) {
                    warningsOut.addWarning(String.format(
                            "Duplicate key '%s' in locale '%s' (langs lpk %s), later value kept",
                            stringEntry.getKey(), locale, sourceLabel));
                }
                target.put(stringEntry.getKey(), stringEntry.getValue());
            }
        }
    }

    private ExtractionResult decodeLpkWithApktool(byte[] lpkBytes, String sourceLabel, String localeHint)
            throws Exception {
        Path tempLpk = Files.createTempFile("apk-lang-", ".lpk");
        Path tempOut = Files.createTempDirectory("apk-lang-lpk-");
        try {
            Files.write(tempLpk, lpkBytes);
            ApktoolResourceExtractor apktool = new ApktoolResourceExtractor(apktoolPath, true);
            ExtractionResult decoded = apktool.extractToDirectory(tempLpk, tempOut);
            if (decoded.localeToStrings().isEmpty()) {
                decoded = ExtractionResult.builder()
                        .addWarning(String.format(
                                "LPK %s: apktool decoded but no strings found (hint locale %s)",
                                sourceLabel, localeHint))
                        .build();
            }
            return decoded;
        } finally {
            Files.deleteIfExists(tempLpk);
            deleteRecursive(tempOut);
        }
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

    static boolean isLangsBrEntry(String zipEntryName) {
        String n = zipEntryName.replace('\\', '/').toLowerCase();
        if (!n.endsWith(".br")) {
            return false;
        }
        return n.startsWith(ASSETS_LANGS_PREFIX) || n.startsWith(LEGACY_LANGS_PREFIX);
    }

    private static boolean isZipMagic(byte[] data) {
        return data.length >= 4
                && data[0] == 'P'
                && data[1] == 'K'
                && (data[2] == 3 || data[2] == 5 || data[2] == 7)
                && (data[3] == 4 || data[3] == 6 || data[3] == 8);
    }

    private static boolean looksLikeLpkPath(String path) {
        return path.toLowerCase().endsWith(".lpk");
    }

    private static void deleteRecursive(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, new java.nio.file.SimpleFileVisitor<>() {
            @Override
            public java.nio.file.FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            @Override
            public java.nio.file.FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }
}
