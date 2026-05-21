package com.apkcompare.lang.extractor;

import com.apkcompare.lang.model.ExtractionResult;
import java.util.Map;
import java.util.TreeMap;

public final class ExtractionMerger {

    private ExtractionMerger() {}

    public static ExtractionResult merge(ExtractionResult primary, ExtractionResult secondary) {
        ExtractionResult.Builder builder = ExtractionResult.builder();
        Map<String, Map<String, String>> merged = new TreeMap<>(primary.localeToStrings());

        for (String warning : primary.warnings()) {
            builder.addWarning(warning);
        }
        for (String warning : secondary.warnings()) {
            builder.addWarning(warning);
        }

        for (Map.Entry<String, Map<String, String>> entry : secondary.localeToStrings().entrySet()) {
            String locale = entry.getKey();
            Map<String, String> incoming = entry.getValue();
            if (!merged.containsKey(locale)) {
                merged.put(locale, new TreeMap<>(incoming));
                continue;
            }
            Map<String, String> existing = merged.get(locale);
            for (Map.Entry<String, String> stringEntry : incoming.entrySet()) {
                if (existing.containsKey(stringEntry.getKey())) {
                    builder.addWarning(String.format(
                            "Duplicate key '%s' in locale '%s' (base APK vs langs lpk), lpk value kept",
                            stringEntry.getKey(), locale));
                }
                existing.put(stringEntry.getKey(), stringEntry.getValue());
            }
        }

        for (Map.Entry<String, Map<String, String>> e : merged.entrySet()) {
            builder.putLocale(e.getKey(), e.getValue());
        }
        return builder.build();
    }
}
