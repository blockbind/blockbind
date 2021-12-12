package dev.cerus.blockbind.api.packet.entity;

import dev.cerus.blockbind.api.packet.Packet;
import io.netty.buffer.ByteBuf;

public class EntityMovePacket extends Packet {

    private int entityId;
    private double oldX;
    private double oldY;
    private double oldZ;
    private double newX;
    private double newY;
    private double newZ;

    public EntityMovePacket(final int entityId, final double oldX, final double oldY, final double oldZ, final double newX, final double newY, final double newZ) {
        this.entityId = entityId;
        this.oldX = oldX;
        this.oldY = oldY;
        this.oldZ = oldZ;
        this.newX = newX;
        this.newY = newY;
        this.newZ = newZ;
    }

    public EntityMovePacket() {
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

}
