package dev.cerus.blockbind.api.packet;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Field;

public abstract class Packet {

    public abstract void write(ByteBuf buffer);

    public abstract void read(ByteBuf buffer);

    @Override
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
