package dev.cerus.blockbind.api.packet.block;

import dev.cerus.blockbind.api.packet.Packet;
import dev.cerus.blockbind.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;

public class BlockChangePacket extends Packet {

    private int id;
    private String worldName;
    private int posX;
    private int posY;
    private int posZ;

    public BlockChangePacket(final int id, final String worldName, final int posX, final int posY, final int posZ) {
        this.id = id;
        this.worldName = worldName;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public BlockChangePacket() {
    }

    @Override
    public void write(final ByteBuf buffer) {
        buffer.writeInt(this.id);
        BufferUtil.writeString(buffer, this.worldName);
        buffer.writeInt(this.posX);
        buffer.writeInt(this.posY);
        buffer.writeInt(this.posZ);
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.id = buffer.readInt();
        this.worldName = BufferUtil.readString(buffer);
        this.posX = buffer.readInt();
        this.posY = buffer.readInt();
        this.posZ = buffer.readInt();
    }

    public int getId() {
        return this.id;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public int getPosX() {
        return this.posX;
    }

    public int getPosY() {
        return this.posY;
    }

    public int getPosZ() {
        return this.posZ;
    }

}
