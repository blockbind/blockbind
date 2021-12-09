package dev.cerus.blockbind.bukkit.listener.packet;

import dev.cerus.blockbind.api.packet.Packet;
import java.util.function.BiConsumer;

public class PlayerPacketListener implements BiConsumer<Packet, Throwable> {

    @Override
    public void accept(final Packet packet, final Throwable throwable) {
    }

}
