package dev.cerus.blockbind.api.compression;

import com.github.luben.zstd.Zstd;

/**
 * Zstd compression implementation using github.com/facebook/zstd and github.com/luben/zstd-jni
 */
public class ZstdCompression implements Compression {

    @Override
    public byte[] compress(final byte[] bytes) {
        return Zstd.compress(bytes);
    }

    @Override
    public byte[] decompress(final byte[] bytes, final int originalSize) {
        return Zstd.decompress(bytes, originalSize);
    }

}
