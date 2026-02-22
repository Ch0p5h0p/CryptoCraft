package org.Ch0p5h0p.cryptocraft.client.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZip {
    public static String compressString(String input) throws IOException {
        if (input == null || input.isEmpty()) return input;

        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(inputBytes);
        }

        byte[] compressedBytes = byteStream.toByteArray();

        return Base64.getEncoder().encodeToString(compressedBytes);
    }

    public static String decompressString(String compressedBase64) throws IOException {
        if (compressedBase64 == null || compressedBase64.isEmpty()) {
            return compressedBase64;
        }

        byte[] compressedBytes = Base64.getDecoder().decode(compressedBase64);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPInputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(compressedBytes))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, len);
            }
        }

        return byteStream.toString(StandardCharsets.UTF_8);
    }
}
