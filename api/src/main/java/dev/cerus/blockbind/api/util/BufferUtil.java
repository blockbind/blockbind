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

/**
 * Common methods for working with buffers
 */
public class BufferUtil {

    private BufferUtil() {
    }

    /**
     * Read a universally unique identifier from a buffer
     *
     * @param buffer The buffer
     *
     * @return A uuid
     */
    public static UUID readUuid(final ByteBuf buffer) {
        final long msb = buffer.readLong();
        final long lsb = buffer.readLong();
        return new UUID(msb, lsb);
    }

    /**
     * Write a universally unique identifier to a buffer
     *
     * @param buffer The buffer
     * @param uuid   The value
     */
    public static void writeUuid(final ByteBuf buffer, final UUID uuid) {
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Read a byte array from a buffer
     *
     * @param buffer The buffer
     *
     * @return The byte array
     */
    public static byte[] read(final ByteBuf buffer) {
        final int len = buffer.readInt();
        final byte[] buf = new byte[len];
        buffer.readBytes(buf);
        return buf;
    }

    /**
     * Write a byte array to a buffer
     *
     * @param buffer The buffer
     * @param arr    The byte array
     */
    public static void write(final ByteBuf buffer, final byte[] arr) {
        buffer.writeInt(arr.length);
        buffer.writeBytes(arr);
    }

    /**
     * Read a string from a byte buffer
     *
     * @param buffer The buffer
     *
     * @return A string
     */
    public static String readString(final ByteBuf buffer) {
        return new String(read(buffer));
    }

    /**
     * Write a string to a byte buffer
     *
     * @param buffer The buffer
     * @param str    The string
     */
    public static void writeString(final ByteBuf buffer, final String str) {
        write(buffer, str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Read a map from a buffer
     *
     * @param buffer The buffer
     * @param keyFun The function for reading keys
     * @param valFun The function for reading values
     * @param <A>    Key
     * @param <B>    Value
     *
     * @return A map
     */
    public static <A, B> Map<A, B> readMap(final ByteBuf buffer,
                                           final Function<ByteBuf, A> keyFun,
                                           final Function<ByteBuf, B> valFun) {
        return readMap(buffer, keyFun, valFun, HashMap::new);
    }

    /**
     * Read a map from a buffer
     *
     * @param buffer    The buffer
     * @param keyFun    The function for reading keys
     * @param valFun    The function for reading values
     * @param mapConstr A constructor function for the map implementation to use
     * @param <A>       Key
     * @param <B>       Value
     * @param <M>       Map implementation
     *
     * @return A map
     */
    public static <A, B, M extends Map<A, B>> M readMap(final ByteBuf buffer,
                                                        final Function<ByteBuf, A> keyFun,
                                                        final Function<ByteBuf, B> valFun,
                                                        final Supplier<M> mapConstr) {
        final M map = mapConstr.get();

        final int len = buffer.readInt();
        for (int i = 0; i < len; i++) {
            map.put(keyFun.apply(buffer), valFun.apply(buffer));
        }

        return map;
    }

    /**
     * Write a map to a buffer
     *
     * @param buffer The buffer
     * @param map    The map
     * @param keyFun The writing function for keys
     * @param valFun The writing function for values
     * @param <A>    Key
     * @param <B>    Value
     */
    public static <A, B> void writeMap(final ByteBuf buffer, final Map<A, B> map,
                                       final BiConsumer<ByteBuf, A> keyFun,
                                       final BiConsumer<ByteBuf, B> valFun) {
        buffer.writeInt(map.size());
        map.forEach((a, b) -> {
            keyFun.accept(buffer, a);
            valFun.accept(buffer, b);
        });
    }

    /**
     * Reads an optional (nullable) value from a buffer
     *
     * @param buffer The buffer
     * @param valFun The reading function for the value
     * @param <T>    Value type
     *
     * @return An optional containing the value if present
     */
    public static <T> Optional<T> readOptional(final ByteBuf buffer, final Function<ByteBuf, T> valFun) {
        final boolean present = buffer.readBoolean();
        if (present) {
            return Optional.of(valFun.apply(buffer));
        }
        return Optional.empty();
    }

    /**
     * Writes an optional to a buffer
     *
     * @param buffer   The buffer
     * @param optional The optional
     * @param valFun   The writing function for the value
     * @param <T>      Value type
     */
    public static <T> void writeOptional(final ByteBuf buffer, final Optional<T> optional, final BiConsumer<ByteBuf, T> valFun) {
        buffer.writeBoolean(optional.isPresent());
        optional.ifPresent(t -> valFun.accept(buffer, t));
    }

    /**
     * Convenience method for reading a player from a buffer
     *
     * @param buffer The buffer
     *
     * @return A player
     */
    public static PlayerWrapper readPlayer(final ByteBuf buffer) {
        return PlayerWrapper.parse(JsonParser.parseString(readString(buffer)).getAsJsonObject());
    }

    /**
     * Convenience method for writing a player to a buffer
     *
     * @param buffer The buffer
     * @param player The player
     */
    public static void writePlayer(final ByteBuf buffer, final PlayerWrapper player) {
        writeString(buffer, player.encode());
    }

}
