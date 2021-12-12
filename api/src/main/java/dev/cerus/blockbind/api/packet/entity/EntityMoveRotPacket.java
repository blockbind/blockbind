package dev.cerus.blockbind.api.packet.entity;

import dev.cerus.blockbind.api.packet.Packet;
import io.netty.buffer.ByteBuf;

public class EntityMoveRotPacket extends Packet {

    private int entityId;
    private double oldX;
    private double oldY;
    private double oldZ;
    private double newX;
    private double newY;
    private double newZ;
    private float yaw;
    private float pitch;

    public EntityMoveRotPacket(final int entityId,
                               final double oldX,
                               final double oldY,
                               final double oldZ,
                               final double newX,
                               final double newY,
                               final double newZ,
                               final float yaw,
                               final float pitch) {
        this.entityId = entityId;
        this.oldX = oldX;
        this.oldY = oldY;
        this.oldZ = oldZ;
        this.newX = newX;
        this.newY = newY;
        this.newZ = newZ;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public EntityMoveRotPacket() {
    }

    @Override
    public void write(final ByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeDouble(this.oldX);
        buffer.writeDouble(this.oldY);
        buffer.writeDouble(this.oldZ);
        buffer.writeDouble(this.newX);
        buffer.writeDouble(this.newY);
        buffer.writeDouble(this.newZ);
        buffer.writeFloat(this.yaw);
        buffer.writeFloat(this.pitch);
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.oldX = buffer.readDouble();
        this.oldY = buffer.readDouble();
        this.oldZ = buffer.readDouble();
        this.newX = buffer.readDouble();
        this.newY = buffer.readDouble();
        this.newZ = buffer.readDouble();
        this.yaw = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    public int getEntityId() {
        return this.entityId;
    }

    public double getOldX() {
        return this.oldX;
    }

    public double getOldY() {
        return this.oldY;
    }

    public double getOldZ() {
        return this.oldZ;
    }

    public double getNewX() {
        return this.newX;
    }

    public double getNewY() {
        return this.newY;
    }

    public double getNewZ() {
        return this.newZ;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

}
