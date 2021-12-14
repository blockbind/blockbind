package dev.cerus.blockbind.api.packet.entity;

import dev.cerus.blockbind.api.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Entity rotation packet
 */
public class EntityRotPacket extends Packet {

    private int entityId;
    private float yaw;
    private float pitch;

    public EntityRotPacket(final int entityId, final float yaw, final float pitch) {
        this.entityId = entityId;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public EntityRotPacket() {
    }

    @Override
    public void write(final ByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeFloat(this.yaw);
        buffer.writeFloat(this.pitch);
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.yaw = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    public int getEntityId() {
        return this.entityId;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

}
