package dev.cerus.blockbind.bukkit.listener.packet;

import dev.cerus.blockbind.api.packet.Packet;
import dev.cerus.blockbind.api.packet.block.BlockChangePacket;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class BlockPacketListener implements BiConsumer<Packet, Throwable> {

    private final BlockBindBukkitPlugin plugin;

    public BlockPacketListener(final BlockBindBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(final Packet packet, final Throwable throwable) {
        if (throwable != null) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not read packet (Block)", throwable);
            return;
        }

        if (packet instanceof BlockChangePacket blockChangePacket) {
            this.handleBlockChange(blockChangePacket);
        }
    }

    private void handleBlockChange(final BlockChangePacket packet) {
        final World world = Bukkit.getWorld(packet.getWorldName());
        if (world != null) {
            Bukkit.getScheduler().runTask(this.plugin, () ->
                    this.plugin.getAdapter().setBlockAt(world.getUID(), packet.getPosX(), packet.getPosY(), packet.getPosZ(), packet.getId()));
        }
    }

}
