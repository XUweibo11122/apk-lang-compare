package com.apkcompare.lang;

/** Entry point for shadow JAR (delegates to {@link ApkLangCli}). */
public final class ApkLangCompareApp {

    private ApkLangCompareApp() {}

    public static void main(String[] args) {
        ApkLangCli.main(args);
    }
}
