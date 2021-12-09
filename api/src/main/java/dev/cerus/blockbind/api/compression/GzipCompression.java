package dev.cerus.blockbind.api.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompression implements Compression {

    @Override
    public byte[] compress(final byte[] bytes) throws IOException {
        final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        final byte[] buf = new byte[512];

        try (final ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
             final GZIPOutputStream gzipOut = new GZIPOutputStream(bOut)) {
            int read;
            while ((read = bIn.read(buf)) != -1) {
                gzipOut.write(buf, 0, read);
            }
            gzipOut.finish();
        }

        return bOut.toByteArray();
    }

    @Override
    public byte[] decompress(final byte[] bytes, final int originalSize) throws IOException {
        final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        final byte[] buf = new byte[512];

        try (final ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
             final GZIPInputStream gzipIn = new GZIPInputStream(bIn)) {
            int read;
            while ((read = gzipIn.read(buf)) != -1) {
                bOut.write(buf, 0, read);
            }
        }

        return bOut.toByteArray();
    }
}
