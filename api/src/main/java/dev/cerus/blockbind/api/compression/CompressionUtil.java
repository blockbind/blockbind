package dev.cerus.blockbind.api.compression;

import java.io.IOException;

/**
 * Utility methods for (de)compressing data based on the implemented compressions
 */
public class CompressionUtil {

    private static boolean zstd;

    private CompressionUtil() {
    }

    /**
     * Compress using the compression implementation that fits the current settings and swallow any IOExceptions
     *
     * @param bytes Byte array to compress
     *
     * @return Compressed bytes
     */
    public static byte[] compressUnsafe(final byte[] bytes) {
        try {
            return compress(bytes);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decompress using the compression implementation that fits the current settings and swallow any IOExceptions
     *
     * @param bytes Byte array to decompress
     *
     * @return Decompressed bytes
     */
    public static byte[] decompressUnsafe(final byte[] bytes, final int originalSize) {
        try {
            return decompress(bytes, originalSize);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Compress using the compression implementation that fits the current settings
     *
     * @param bytes Byte array to compress
     *
     * @return Compressed bytes
     *
     * @throws IOException Depending on the implementation
     */
    public static byte[] compress(final byte[] bytes) throws IOException {
        final Compression compression = zstd ? Compression.ZSTD : Compression.GZIP;
        return compression.compress(bytes);
    }

    /**
     * Decompress using the compression implementation that fits the current settings
     *
     * @param bytes        Byte array to decompress
     * @param originalSize Original size of the decompressed data
     *
     * @return Decompressed bytes
     *
     * @throws IOException Depending on the implementation
     */
    public static byte[] decompress(final byte[] bytes, final int originalSize) throws IOException {
        final Compression compression = zstd ? Compression.ZSTD : Compression.GZIP;
        return compression.decompress(bytes, originalSize);
    }

    /**
     * Is Zstd enabled?
     *
     * @return True or false
     */
    public static boolean isZstd() {
        return zstd;
    }

    /**
     * Disable / Enable Zstd compression
     *
     * @param zstd Enable or disable
     */
    public static void setZstd(final boolean zstd) {
        CompressionUtil.zstd = zstd;
    }

}
