package com.apkcompare.lang;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DumpCommandTest {

    @Test
    void defaultDecodeDirBesideApk(@TempDir Path temp) throws Exception {
        Path apk = temp.resolve("sample.apk");
        Files.writeString(apk, "fake");

        Path dir = DumpCommand.resolveDecodeDir(apk, null);
        assertTrue(dir.endsWith("sample-decoded"));
        assertTrue(Files.isDirectory(dir));
    }
}
