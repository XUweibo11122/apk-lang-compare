package com.apkcompare.lang.extractor;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

final class BrotliUtil {

    private BrotliUtil() {}

    static byte[] decompress(byte[] compressed) throws IOException {
        Brotli4jLoader.ensureAvailability();
        try (InputStream in = new BrotliInputStream(new ByteArrayInputStream(compressed));
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toByteArray();
        }
    }

    static byte[] decompress(InputStream compressed) throws IOException {
        return decompress(compressed.readAllBytes());
    }
}
