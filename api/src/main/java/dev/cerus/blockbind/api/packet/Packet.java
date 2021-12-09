package dev.cerus.blockbind.api.packet;

import io.netty.buffer.ByteBuf;

public abstract class Packet {

    public abstract void write(ByteBuf buffer);

    public abstract void read(ByteBuf buffer);

}
