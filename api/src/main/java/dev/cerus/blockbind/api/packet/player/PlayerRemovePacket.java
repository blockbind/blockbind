package dev.cerus.blockbind.api.packet.player;

import dev.cerus.blockbind.api.packet.Packet;
import dev.cerus.blockbind.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public class PlayerRemovePacket extends Packet {

    private UUID uuid;

    public PlayerRemovePacket(final UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerRemovePacket() {
    }

    @Override
    public void write(final ByteBuf buffer) {
        BufferUtil.writeUuid(buffer, this.uuid);
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.uuid = BufferUtil.readUuid(buffer);
    }

    public UUID getUuid() {
        return this.uuid;
    }

}
