package com.apkcompare.lang.model;

import java.util.Collections;
import java.util.List;

public final class LocaleDiff {

    private final List<String> onlyInApk1;
    private final List<String> onlyInApk2;

    public LocaleDiff(List<String> onlyInApk1, List<String> onlyInApk2) {
        this.onlyInApk1 = List.copyOf(onlyInApk1);
        this.onlyInApk2 = List.copyOf(onlyInApk2);
    }

    public List<String> onlyInApk1() {
        return onlyInApk1;
    }

    public List<String> onlyInApk2() {
        return onlyInApk2;
    }

    public boolean hasDifferences() {
        return !onlyInApk1.isEmpty() || !onlyInApk2.isEmpty();
    }
}
