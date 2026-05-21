package com.apkcompare.lang.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.apkcompare.lang.compare.ResourceComparator;
import com.apkcompare.lang.model.ExtractionResult;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

class LpkZipStringScannerTest {

    @Test
    void resourcesArscOnlyDoesNotWarnAboutMissingXml() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            ZipEntry entry = new ZipEntry("resources.arsc");
            zos.putNextEntry(entry);
            zos.write(new byte[] {0x02, 0x00, 0x0c, 0x00});
            zos.closeEntry();
        }
        ExtractionResult result = LpkZipStringScanner.scan(bos.toByteArray(), "base-vi.lpk", "vi");
        assertEquals(0, ResourceComparator.countStrings(result.localeToStrings()));
        assertTrue(result.warnings().isEmpty());
    }
}
