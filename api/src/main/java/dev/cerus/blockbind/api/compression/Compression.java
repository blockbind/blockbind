package dev.cerus.blockbind.api.compression;

import java.io.IOException;

/**
 * Represents a compression algorithm
 */
public interface Compression {

    /**
     * Instance of the Zstd compression implementation
     */
    Compression ZSTD = new ZstdCompression();

    /**
     * Instance of the Gzip compression implementation
     */
    Compression GZIP = new GzipCompression();

    /**
     * Compresses the provided byte array
     *
     * @param bytes The bytes to compress
     *
     * @return The compressed bytes
     *
     * @throws IOException Depending on the implementation
     */
    byte[] compress(byte[] bytes) throws IOException;

    /**
     * Decompressed the provided byte array
     *
     * @param bytes        The bytes to decompress
     * @param originalSize The original size of the uncompressed data
     *
     * @return The decompressed bytes
     *
     * @throws IOException Depending on the implementation
     */
    byte[] decompress(byte[] bytes, int originalSize) throws IOException;

}
