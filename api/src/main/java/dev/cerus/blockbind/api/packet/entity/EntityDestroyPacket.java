package dev.cerus.blockbind.api.packet.entity;

import dev.cerus.blockbind.api.packet.Packet;
import io.netty.buffer.ByteBuf;

public class EntityDestroyPacket extends Packet {

    private int receiver;
    private int[] ids;

    public EntityDestroyPacket(final int receiver, final int[] ids) {
        this.receiver = receiver;
        this.ids = ids;
    }

    public EntityDestroyPacket() {
    }

    @Override
    public void write(final ByteBuf buffer) {
        buffer.writeInt(this.receiver);
        buffer.writeInt(this.ids.length);
        for (final int id : this.ids) {
            buffer.writeInt(id);
        }
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.receiver = buffer.readInt();
        this.ids = new int[buffer.readInt()];
        for (int i = 0; i < this.ids.length; i++) {
            this.ids[i] = buffer.readInt();
        }
    }

    public int getReceiver() {
        return this.receiver;
    }

    public int[] getIds() {
        return this.ids;
    }

}
