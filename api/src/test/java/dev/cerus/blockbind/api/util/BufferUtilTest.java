package dev.cerus.blockbind.api.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

public class BufferUtilTest {

    @RepeatedTest(10)
    public void testUuid() {
        final Random random = new Random();
        final ByteBuf buffer = Unpooled.buffer();
        final UUID uuid = new UUID(random.nextLong(), random.nextLong());
        BufferUtil.writeUuid(buffer, uuid);
        buffer.resetReaderIndex();
        final UUID readUuid = BufferUtil.readUuid(buffer);
        buffer.release();
        Assertions.assertEquals(uuid, readUuid);
    }

    @RepeatedTest(10)
    public void testString() {
        final Random random = new Random();
        final ByteBuf buffer = Unpooled.buffer();
        final String str = IntStream.range(0, 64)
                .mapToObj($ -> (char) (random.nextInt('z' - 'a') + 'a'))
                .map(String::valueOf)
                .collect(Collectors.joining());
        BufferUtil.writeString(buffer, str);
        buffer.resetReaderIndex();
        final String readStr = BufferUtil.readString(buffer);
        buffer.release();
        Assertions.assertEquals(str, readStr);
    }

    @RepeatedTest(10)
    public void testMap() {
        final Random random = new Random();
        final ByteBuf buffer = Unpooled.buffer();
        final Map<String, String> map = new HashMap<>();
        final int size = random.nextInt(100);
        for (int j = 0; j < size; j++) {
            map.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }
        BufferUtil.writeMap(
                buffer,
                map,
                ($, key) -> BufferUtil.writeString(buffer, key),
                ($, val) -> BufferUtil.writeString(buffer, val)
        );
        buffer.resetReaderIndex();
        final Map<String, String> readMap = BufferUtil.readMap(
                buffer,
                $ -> BufferUtil.readString(buffer),
                $ -> BufferUtil.readString(buffer)
        );
        buffer.release();
        Assertions.assertEquals(map, readMap);
    }

}
