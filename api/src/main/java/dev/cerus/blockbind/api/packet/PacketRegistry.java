package dev.cerus.blockbind.api.packet;

import dev.cerus.blockbind.api.packet.player.PlayerInfoPacket;
import java.util.Map;
import java.util.function.Supplier;

public class PacketRegistry {

    private static final Map<Class<? extends Packet>, Integer> PACKET_ID_MAP = Map.of(
            PlayerInfoPacket.class, 0x00
    );

    private static final Map<Integer, Supplier<? extends Packet>> ID_PACKET_MAP = Map.of(
            0x00, PlayerInfoPacket::new
    );

    private PacketRegistry() {
    }

    public static int getPacketId(final Class<? extends Packet> clazz) {
        return PACKET_ID_MAP.get(clazz);
    }

    public static <P extends Packet> P getPacketById(final int id) {
        if (!ID_PACKET_MAP.containsKey(id)) {
            throw new NullPointerException();
        }
        return (P) ID_PACKET_MAP.get(id).get();
    }

}
