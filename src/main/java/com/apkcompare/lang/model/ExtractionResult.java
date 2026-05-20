package com.apkcompare.lang.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class ExtractionResult {

    private final Map<String, Map<String, String>> localeToStrings;
    private final List<String> warnings;

    public ExtractionResult(Map<String, Map<String, String>> localeToStrings, List<String> warnings) {
        this.localeToStrings = Collections.unmodifiableMap(new TreeMap<>(localeToStrings));
        this.warnings = List.copyOf(warnings);
    }

    public Map<String, Map<String, String>> localeToStrings() {
        return localeToStrings;
    }

    public List<String> warnings() {
        return warnings;
    }

    public static ExtractionResult empty() {
        return new ExtractionResult(Map.of(), List.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, Map<String, String>> localeToStrings = new TreeMap<>();
        private final List<String> warnings = new ArrayList<>();

        public Builder putLocale(String locale, Map<String, String> strings) {
            localeToStrings.put(locale, new TreeMap<>(strings));
            return this;
        }

        public Builder addWarning(String warning) {
            warnings.add(warning);
            return this;
        }

        public ExtractionResult build() {
            return new ExtractionResult(localeToStrings, warnings);
        }
    }
}
