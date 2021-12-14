package dev.cerus.blockbind.bukkit.listener.server;

import dev.cerus.blockbind.api.packet.entity.EntityMetadataPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Bukkit listener for player crouching
 */
public class PlayerCrouchListener implements Listener {

    private final BlockBindBukkitPlugin plugin;
    private final PacketRedisCommunicator packetCommunicator;

    public PlayerCrouchListener(final BlockBindBukkitPlugin plugin, final PacketRedisCommunicator packetCommunicator) {
        this.plugin = plugin;
        this.packetCommunicator = packetCommunicator;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSneak(final PlayerToggleSneakEvent event) {
        final Player player = event.getPlayer();
        final PlayerWrapper wrapper = this.plugin.getUuidPlayerMap().get(player.getUniqueId());
        if (wrapper != null) {
            wrapper.setCrouching(event.isSneaking());
            wrapper.setPose(event.isSneaking() ? (byte) Pose.SNEAKING.ordinal() : (byte) Pose.STANDING.ordinal());
            this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityMetadataPacket(
                    wrapper.getEntityId(),
                    wrapper.getMetadata()
            ));
            wrapper.getMetadata().clean();
        }
    }

}
