package com.apkcompare.lang;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "apk-lang-compare",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "APK multi-language string tools: compare two APKs or dump one APK to JSON.",
        subcommands = {CompareCommand.class, DumpCommand.class})
public class ApkLangCli implements Runnable {

    public static final int EXIT_OK = 0;
    public static final int EXIT_IDENTICAL = 0;
    public static final int EXIT_DIFFERENT = 1;
    public static final int EXIT_ERROR = 2;

    public static void main(String[] args) {
        try {
            com.aayushatharva.brotli4j.Brotli4jLoader.ensureAvailability();
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Warning: Brotli native library unavailable; assets/langs/*.br extraction may fail.");
        }
        CommandLine cmd = new CommandLine(new ApkLangCli());
        cmd.setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            System.err.println("Error: " + cause.getMessage());
            return EXIT_ERROR;
        });
        int exitCode = cmd.execute(normalizeArgs(args));
        System.exit(exitCode);
    }

    /** Legacy: `jar a.apk b.apk` → `jar compare a.apk b.apk` */
    static String[] normalizeArgs(String[] args) {
        if (args.length == 0) {
            return args;
        }
        String first = args[0];
        if ("compare".equals(first) || "dump".equals(first) || first.startsWith("-")) {
            return args;
        }
        if (first.toLowerCase().endsWith(".apk")) {
            String[] shifted = new String[args.length + 1];
            shifted[0] = "compare";
            System.arraycopy(args, 0, shifted, 1, args.length);
            return shifted;
        }
        return args;
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
