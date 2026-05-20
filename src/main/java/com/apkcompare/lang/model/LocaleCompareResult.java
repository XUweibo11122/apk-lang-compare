package com.apkcompare.lang.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LocaleCompareResult {

    private final List<String> onlyInApk1;
    private final List<String> onlyInApk2;
    private final List<ValueMismatch> valueMismatch;

    public LocaleCompareResult(
            List<String> onlyInApk1,
            List<String> onlyInApk2,
            List<ValueMismatch> valueMismatch) {
        this.onlyInApk1 = List.copyOf(onlyInApk1);
        this.onlyInApk2 = List.copyOf(onlyInApk2);
        this.valueMismatch = List.copyOf(valueMismatch);
    }

    public List<String> onlyInApk1() {
        return onlyInApk1;
    }

    public List<String> onlyInApk2() {
        return onlyInApk2;
    }

    public List<ValueMismatch> valueMismatch() {
        return valueMismatch;
    }

    public boolean hasDifferences() {
        return !onlyInApk1.isEmpty() || !onlyInApk2.isEmpty() || !valueMismatch.isEmpty();
    }

    public static LocaleCompareResult empty() {
        return new LocaleCompareResult(List.of(), List.of(), List.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<String> onlyInApk1 = new ArrayList<>();
        private final List<String> onlyInApk2 = new ArrayList<>();
        private final List<ValueMismatch> valueMismatch = new ArrayList<>();

        public Builder onlyInApk1(List<String> keys) {
            onlyInApk1.clear();
            onlyInApk1.addAll(keys);
            Collections.sort(onlyInApk1);
            return this;
        }

        public Builder onlyInApk2(List<String> keys) {
            onlyInApk2.clear();
            onlyInApk2.addAll(keys);
            Collections.sort(onlyInApk2);
            return this;
        }

        public Builder valueMismatch(List<ValueMismatch> mismatches) {
            valueMismatch.clear();
            valueMismatch.addAll(mismatches);
            return this;
        }

        public LocaleCompareResult build() {
            return new LocaleCompareResult(onlyInApk1, onlyInApk2, valueMismatch);
        }
    }
}
