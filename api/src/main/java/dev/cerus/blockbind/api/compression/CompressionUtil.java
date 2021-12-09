package dev.cerus.blockbind.api.compression;

import java.io.IOException;

public class CompressionUtil {

    private static boolean zstd;

    private CompressionUtil() {
    }

    public static byte[] compressUnsafe(final byte[] bytes) {
        try {
            return compress(bytes);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decompressUnsafe(final byte[] bytes, final int originalSize) {
        try {
            return decompress(bytes, originalSize);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] compress(final byte[] bytes) throws IOException {
        final Compression compression = zstd ? Compression.ZSTD : Compression.GZIP;
        return compression.compress(bytes);
    }

    public static byte[] decompress(final byte[] bytes, final int originalSize) throws IOException {
        final Compression compression = zstd ? Compression.ZSTD : Compression.GZIP;
        return compression.decompress(bytes, originalSize);
    }

    public static boolean isZstd() {
        return zstd;
    }

    public static void setZstd(final boolean zstd) {
        CompressionUtil.zstd = zstd;
    }

}
