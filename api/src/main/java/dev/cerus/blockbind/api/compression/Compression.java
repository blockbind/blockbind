package dev.cerus.blockbind.api.compression;

import java.io.IOException;

public interface Compression {

    Compression ZSTD = new ZstdCompression();
    Compression GZIP = new GzipCompression();

    byte[] compress(byte[] bytes) throws IOException;

    byte[] decompress(byte[] bytes, int originalSize) throws IOException;

}
