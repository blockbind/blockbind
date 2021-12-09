package dev.cerus.blockbind.api.compression;

import java.io.IOException;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class ZstdCompressionTest {

    @Test
    public void testZstd() throws IOException {
        final Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final byte[] bytes = new byte[5 * 1024];
            for (int j = 0; j < bytes.length; j++) {
                bytes[j] = (byte) (random.nextInt(255) - 128);
            }

            final byte[] compressed = Compression.ZSTD.compress(bytes);
            final byte[] decompressed = Compression.ZSTD.decompress(compressed, bytes.length);
            Assert.assertArrayEquals("Decompressed bytes do not match original bytes", bytes, decompressed);
        }
    }

}
