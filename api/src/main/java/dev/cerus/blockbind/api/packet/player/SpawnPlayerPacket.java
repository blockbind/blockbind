package dev.cerus.blockbind.api.packet.player;

import dev.cerus.blockbind.api.packet.Packet;
import dev.cerus.blockbind.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public class SpawnPlayerPacket extends Packet {

    private int receiver;
    private int entityId;
    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public SpawnPlayerPacket(final int receiver,
                             final int entityId,
                             final UUID uuid,
                             final double x,
                             final double y,
                             final double z,
                             final float yaw,
                             final float pitch) {
        this.receiver = receiver;
        this.entityId = entityId;
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public SpawnPlayerPacket() {
    }

    @Override
    public void write(final ByteBuf buffer) {
        buffer.writeInt(this.receiver);
        buffer.writeInt(this.entityId);
        BufferUtil.writeUuid(buffer, this.uuid);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        buffer.writeFloat(this.yaw);
        buffer.writeFloat(this.pitch);
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.receiver = buffer.readInt();
        this.entityId = buffer.readInt();
        this.uuid = BufferUtil.readUuid(buffer);
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.yaw = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    public int getReceiver() {
        return this.receiver;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

}
