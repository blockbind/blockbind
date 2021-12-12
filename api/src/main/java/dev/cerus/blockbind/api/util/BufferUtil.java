package dev.cerus.blockbind.api.util;

import com.google.gson.JsonParser;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BufferUtil {

    private BufferUtil() {
    }

    public static UUID readUuid(final ByteBuf buffer) {
        final long msb = buffer.readLong();
        final long lsb = buffer.readLong();
        return new UUID(msb, lsb);
    }

    public static void writeUuid(final ByteBuf buffer, final UUID uuid) {
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());
    }

    public static byte[] read(final ByteBuf buffer) {
        final int len = buffer.readInt();
        final byte[] buf = new byte[len];
        buffer.readBytes(buf);
        return buf;
    }

    public static void write(final ByteBuf buffer, final byte[] arr) {
        buffer.writeInt(arr.length);
        buffer.writeBytes(arr);
    }

    public static String readString(final ByteBuf buffer) {
        return new String(read(buffer));
    }

    public static void writeString(final ByteBuf buffer, final String str) {
        write(buffer, str.getBytes(StandardCharsets.UTF_8));
    }

    public static <A, B> Map<A, B> readMap(final ByteBuf buffer,
                                           final Function<ByteBuf, A> keyFun,
                                           final Function<ByteBuf, B> valFun) {
        return readMap(buffer, keyFun, valFun, HashMap::new);
    }

    public static <A, B> Map<A, B> readMap(final ByteBuf buffer,
                                           final Function<ByteBuf, A> keyFun,
                                           final Function<ByteBuf, B> valFun,
                                           final Supplier<Map<A, B>> mapConstr) {
        final Map<A, B> map = mapConstr.get();

        final int len = buffer.readInt();
        for (int i = 0; i < len; i++) {
            map.put(keyFun.apply(buffer), valFun.apply(buffer));
        }

        return map;
    }

    public static <A, B> void writeMap(final ByteBuf buffer, final Map<A, B> map,
                                       final BiConsumer<ByteBuf, A> keyFun,
                                       final BiConsumer<ByteBuf, B> valFun) {
        buffer.writeInt(map.size());
        map.forEach((a, b) -> {
            keyFun.accept(buffer, a);
            valFun.accept(buffer, b);
        });
    }

    public static <T> Optional<T> readOptional(final ByteBuf buffer, final Function<ByteBuf, T> valFun) {
        final boolean present = buffer.readBoolean();
        if (present) {
            return Optional.of(valFun.apply(buffer));
        }
        return Optional.empty();
    }

    public static <T> void writeOptional(final ByteBuf buffer, final Optional<T> optional, final BiConsumer<ByteBuf, T> valFun) {
        buffer.writeBoolean(optional.isPresent());
        optional.ifPresent(t -> valFun.accept(buffer, t));
    }

    public static PlayerWrapper readPlayer(final ByteBuf buffer) {
        return PlayerWrapper.parse(JsonParser.parseString(readString(buffer)).getAsJsonObject());
    }

    public static void writePlayer(final ByteBuf buffer, final PlayerWrapper player) {
        writeString(buffer, player.encode());
    }

}
