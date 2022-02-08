package dev.cerus.blockbind.bukkit.listener.server;

import dev.cerus.blockbind.api.packet.block.BlockChangePacket;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlayerChangeBlockListener implements Listener {

    private final BlockBindBukkitPlugin plugin;
    private final PacketRedisCommunicator packetCommunicator;

    public PlayerChangeBlockListener(final BlockBindBukkitPlugin plugin, final PacketRedisCommunicator packetCommunicator) {
        this.plugin = plugin;
        this.packetCommunicator = packetCommunicator;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Block block = event.getBlockPlaced();
        this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_BLOCK, new BlockChangePacket(
                this.plugin.getAdapter().getProtocolId(
                        block.getWorld().getUID(),
                        block.getX(), block.getY(), block.getZ()
                ),
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        ));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_BLOCK, new BlockChangePacket(
                0,
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        ));
    }

}
