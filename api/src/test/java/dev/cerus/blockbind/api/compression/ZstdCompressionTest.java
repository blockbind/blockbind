package dev.cerus.blockbind.api.compression;

import java.io.IOException;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

public class ZstdCompressionTest {

    @RepeatedTest(10)
    public void testZstd() throws IOException {
        final Random random = new Random();
        final byte[] bytes = new byte[5 * 1024];
        for (int j = 0; j < bytes.length; j++) {
            bytes[j] = (byte) (random.nextInt(255) - 128);
        }

        final byte[] compressed = Compression.ZSTD.compress(bytes);
        final byte[] decompressed = Compression.ZSTD.decompress(compressed, bytes.length);
        Assertions.assertArrayEquals(bytes, decompressed);
    }

}
