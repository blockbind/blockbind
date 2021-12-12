package dev.cerus.blockbind.api.packet.player;

import dev.cerus.blockbind.api.packet.Packet;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public class PlayerInfoPacket extends Packet {

    private Type type;
    private UUID uuid;

    public PlayerInfoPacket(final Type type,
                            final UUID uuid) {
        this.type = type;
        this.uuid = uuid;
    }

    public PlayerInfoPacket() {
    }

    public static PlayerInfoPacket addPlayer(final PlayerWrapper player) {
        return makePacket(player, Type.ADD_PLAYER);
    }

    public static PlayerInfoPacket removePlayer(final PlayerWrapper player) {
        return makePacket(player, Type.REMOVE_PLAYER);
    }

    public static PlayerInfoPacket updateLatency(final PlayerWrapper player) {
        return makePacket(player, Type.UPDATE_LATENCY);
    }

    public static PlayerInfoPacket updateGameMode(final PlayerWrapper player) {
        return makePacket(player, Type.UPDATE_GAMEMODE);
    }

    public static PlayerInfoPacket updateDisplayName(final PlayerWrapper player) {
        return makePacket(player, Type.UPDATE_DISPLAYNAME);
    }

    private static PlayerInfoPacket makePacket(final PlayerWrapper player, final Type type) {
        return new PlayerInfoPacket(
                type,
                player.getUuid()
        );
    }

    @Override
    public void write(final ByteBuf buffer) {
        buffer.writeByte(this.type.id);
        BufferUtil.writeUuid(buffer, this.uuid);
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.type = Type.getById(buffer.readByte());
        this.uuid = BufferUtil.readUuid(buffer);
    }

    public Type getType() {
        return this.type;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public enum Type {
        ADD_PLAYER(0), REMOVE_PLAYER(1), UPDATE_GAMEMODE(2), UPDATE_LATENCY(3), UPDATE_DISPLAYNAME(4);

        private final int id;

        Type(final int id) {
            this.id = id;
        }

        public static Type getById(final int id) {
            for (final Type value : values()) {
                if (value.id == id) {
                    return value;
                }
            }
            return null;
        }

    }

}
