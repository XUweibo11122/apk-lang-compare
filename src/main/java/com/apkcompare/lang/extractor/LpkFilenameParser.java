package com.apkcompare.lang.extractor;

/**
 * Maps {@code zh-rCN.lpk} / {@code zh_rCN.lpk} style names to locale tags.
 */
public final class LpkFilenameParser {

    private LpkFilenameParser() {}

    public static String toLocaleTag(String lpkFileName) {
        String base = lpkFileName;
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        base = base.replace('_', '-');
        if (base.startsWith("base-")) {
            base = base.substring("base-".length());
        }
        if ("default".equalsIgnoreCase(base) || "values".equalsIgnoreCase(base)) {
            return "default";
        }
        String normalized = base;
        int region = normalized.indexOf("-r");
        if (region > 0) {
            normalized = normalized.substring(0, region) + "-" + normalized.substring(region + 2);
        }
        return normalized;
    }
}
