package dev.cerus.blockbind.api.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Implementation of the Gzip compression using already existing Java classes
 */
public class GzipCompression implements Compression {

    @Override
    public byte[] compress(final byte[] bytes) throws IOException {
        // Create output stream, create 512b buffer
        final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        final byte[] buf = new byte[512];

        // Read byte array
        try (final ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
             final GZIPOutputStream gzipOut = new GZIPOutputStream(bOut)) {
            // Fill buffer
            int read;
            while ((read = bIn.read(buf)) != -1) {
                // Write buffer to output stream
                gzipOut.write(buf, 0, read);
            }
            // Don't forget to write the trailer
            gzipOut.finish();
        }

        return bOut.toByteArray();
    }

    @Override
    public byte[] decompress(final byte[] bytes, final int originalSize) throws IOException {
        // Create output stream, create 512b buffer
        final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        final byte[] buf = new byte[512];

        try (final ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
             final GZIPInputStream gzipIn = new GZIPInputStream(bIn)) {
            // Fill buffer
            int read;
            while ((read = gzipIn.read(buf)) != -1) {
                // Write buffer to output stream
                bOut.write(buf, 0, read);
            }
        }

        return bOut.toByteArray();
    }

}
