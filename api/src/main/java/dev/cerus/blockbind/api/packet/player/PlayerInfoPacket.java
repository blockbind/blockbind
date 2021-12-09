package dev.cerus.blockbind.api.packet.player;

import dev.cerus.blockbind.api.packet.Packet;
import dev.cerus.blockbind.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.UUID;

public class PlayerInfoPacket extends Packet {

    private UUID uuid;
    private String name;
    private Map<String, String> properties;

    public PlayerInfoPacket(final UUID uuid, final String name, final Map<String, String> properties) {
        this.uuid = uuid;
        this.name = name;
        this.properties = properties;
    }

    public PlayerInfoPacket() {
    }

    @Override
    public void write(final ByteBuf buffer) {
        BufferUtil.writeUuid(buffer, this.uuid);
        BufferUtil.writeString(buffer, this.name);
        BufferUtil.writeMap(
                buffer,
                this.properties,
                ($, key) -> BufferUtil.writeString(buffer, key),
                ($, val) -> BufferUtil.writeString(buffer, val)
        );
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.uuid = BufferUtil.readUuid(buffer);
        this.name = BufferUtil.readString(buffer);
        this.properties = BufferUtil.readMap(
                buffer,
                $ -> BufferUtil.readString(buffer),
                $ -> BufferUtil.readString(buffer)
        );
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

}
