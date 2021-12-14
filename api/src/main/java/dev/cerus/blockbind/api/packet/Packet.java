package dev.cerus.blockbind.api.packet;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Field;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Packets are used for cross server communication. The Block Bind protocol is very similar to the Minecraft protocol.
 */
public abstract class Packet {

    /**
     * Write this packet to a buffer
     *
     * @param buffer The buffer
     */
    public abstract void write(ByteBuf buffer);

    /**
     * Read the values for this packet from a buffer
     *
     * @param buffer The buffer
     */
    public abstract void read(ByteBuf buffer);

    @Override
    @VisibleForTesting
    public String toString() {
        final StringBuilder builder = new StringBuilder(this.getClass().getSimpleName()).append("{");
        final Field[] fields = this.getClass().getDeclaredFields();
        for (final Field field : fields) {
            try {
                field.setAccessible(true);
                builder.append(field.getName()).append("=").append(field.get(this)).append(",");
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return builder.append("}").toString();
    }

}
