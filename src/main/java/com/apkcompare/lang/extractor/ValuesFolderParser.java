package com.apkcompare.lang.extractor;

import java.util.regex.Pattern;

/**
 * Maps Android {@code res/values*} directory names to locale tags used in reports.
 */
public final class ValuesFolderParser {

    /** Language with optional -rREGION prefix at start of a qualifier chain (e.g. zh-rCN-port). */
    private static final Pattern LOCALE_PREFIX =
            Pattern.compile("^([a-z]{2,3}(?:-r[A-Z]{2})?)(?:-|$)");

    private ValuesFolderParser() {}

    public static boolean isValuesFolder(String dirName) {
        return "values".equals(dirName) || dirName.startsWith("values-");
    }

    public static String toLocaleTag(String valuesDirName) {
        if ("values".equals(valuesDirName)) {
            return "default";
        }
        if (!valuesDirName.startsWith("values-")) {
            return valuesDirName;
        }
        String qualifiers = valuesDirName.substring("values-".length());
        String localePart = extractLocaleQualifier(qualifiers);
        if (localePart == null) {
            return qualifiers;
        }
        return normalizeLocalePart(localePart);
    }

    /**
     * Extracts the language/locale prefix from a values-* qualifier chain (e.g. zh-rCN from zh-rCN-port).
     */
    static String extractLocaleQualifier(String qualifiers) {
        var matcher = LOCALE_PREFIX.matcher(qualifiers);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    static String normalizeLocalePart(String localePart) {
        int regionIdx = localePart.indexOf("-r");
        if (regionIdx < 0) {
            return localePart;
        }
        String language = localePart.substring(0, regionIdx);
        String region = localePart.substring(regionIdx + 2);
        return language + "-" + region;
    }
}
