package dev.cerus.blockbind.bukkit.listener.server;

import dev.cerus.blockbind.api.packet.entity.EntityMovePacket;
import dev.cerus.blockbind.api.packet.entity.EntityMoveRotPacket;
import dev.cerus.blockbind.api.packet.entity.EntityRotPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final BlockBindBukkitPlugin plugin;
    private final PacketRedisCommunicator packetCommunicator;

    public PlayerMoveListener(final BlockBindBukkitPlugin plugin, final PacketRedisCommunicator packetCommunicator) {
        this.plugin = plugin;
        this.packetCommunicator = packetCommunicator;
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final PlayerWrapper wrapper = this.plugin.getUuidPlayerMap().get(player.getUniqueId());
        if (wrapper == null) {
            return;
        }

        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            // Look
            this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityRotPacket(
                    wrapper.getEntityId(),
                    to.getYaw(),
                    to.getPitch()
            ));
            wrapper.setYaw(to.getYaw());
            wrapper.setPitch(to.getPitch());
        } else if (from.getYaw() == to.getYaw() && from.getPitch() == to.getPitch()) {
            // Rel move
            this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityMovePacket(
                    wrapper.getEntityId(),
                    from.getX(),
                    from.getY(),
                    from.getZ(),
                    to.getX(),
                    to.getY(),
                    to.getZ()
            ));
            wrapper.setX(to.getX());
            wrapper.setY(to.getY());
            wrapper.setZ(to.getZ());
        } else {
            // Rel move look
            this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityMoveRotPacket(
                    wrapper.getEntityId(),
                    from.getX(),
                    from.getY(),
                    from.getZ(),
                    to.getX(),
                    to.getY(),
                    to.getZ(),
                    to.getYaw(),
                    to.getPitch()
            ));
            wrapper.setX(to.getX());
            wrapper.setY(to.getY());
            wrapper.setZ(to.getZ());
            wrapper.setYaw(to.getYaw());
            wrapper.setPitch(to.getPitch());
        }
    }

}
