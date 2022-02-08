package dev.cerus.blockbind.api.packet;

import dev.cerus.blockbind.api.packet.block.BlockChangePacket;
import dev.cerus.blockbind.api.packet.entity.EntityDestroyPacket;
import dev.cerus.blockbind.api.packet.entity.EntityMetadataPacket;
import dev.cerus.blockbind.api.packet.entity.EntityMovePacket;
import dev.cerus.blockbind.api.packet.entity.EntityMoveRotPacket;
import dev.cerus.blockbind.api.packet.entity.EntityRotPacket;
import dev.cerus.blockbind.api.packet.player.PlayerInfoPacket;
import dev.cerus.blockbind.api.packet.player.SpawnPlayerPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registry for all the available Block Bind packets
 */
public class PacketRegistry {

    private static final Map<Class<? extends Packet>, Integer> PACKET_ID_MAP = new HashMap<>();
    private static final Map<Integer, Supplier<? extends Packet>> ID_PACKET_MAP = new HashMap<>();

    static {
        // Register packets
        register(0x00, PlayerInfoPacket.class, PlayerInfoPacket::new);
        register(0x01, SpawnPlayerPacket.class, SpawnPlayerPacket::new);
        register(0x02, EntityMovePacket.class, EntityMovePacket::new);
        register(0x03, EntityMoveRotPacket.class, EntityMoveRotPacket::new);
        register(0x04, EntityRotPacket.class, EntityRotPacket::new);
        register(0x05, EntityDestroyPacket.class, EntityDestroyPacket::new);
        register(0x06, EntityMetadataPacket.class, EntityMetadataPacket::new);
        register(0x07, BlockChangePacket.class, BlockChangePacket::new);
    }

    private PacketRegistry() {
    }

    /**
     * Registers a packet
     *
     * @param id     The packet id
     * @param clazz  The class of the packet
     * @param constr A constructor function for the packet
     * @param <P>    Packet type
     */
    private static <P extends Packet> void register(final int id, final Class<P> clazz, final Supplier<P> constr) {
        PACKET_ID_MAP.put(clazz, id);
        ID_PACKET_MAP.put(id, constr);
    }

    /**
     * Get the corresponding packet id
     *
     * @param clazz Class of the packet
     *
     * @return The packet id
     */
    public static int getPacketId(final Class<? extends Packet> clazz) {
        return PACKET_ID_MAP.get(clazz);
    }

    /**
     * Constructs a new packet matching the provided id
     *
     * @param id  The packet id
     * @param <P> Packet type
     *
     * @return A new matching packet
     */
    public static <P extends Packet> P getPacketById(final int id) {
        if (!ID_PACKET_MAP.containsKey(id)) {
            throw new NullPointerException();
        }
        return (P) ID_PACKET_MAP.get(id).get();
    }

}
