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
import org.bukkit.event.entity.EntityToggleGlideEvent;

/**
 * Bukkit listener for player gliding
 */
public class PlayerElytraListener implements Listener {

    private final BlockBindBukkitPlugin plugin;
    private final PacketRedisCommunicator packetCommunicator;

    public PlayerElytraListener(final BlockBindBukkitPlugin plugin, final PacketRedisCommunicator packetCommunicator) {
        this.plugin = plugin;
        this.packetCommunicator = packetCommunicator;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSneak(final EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        final PlayerWrapper wrapper = this.plugin.getUuidPlayerMap().get(player.getUniqueId());
        if (wrapper != null) {
            wrapper.setElytraFlying(event.isGliding());
            wrapper.setPose(event.isGliding() ? (byte) Pose.FALL_FLYING.ordinal() : (byte) Pose.STANDING.ordinal());
            this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityMetadataPacket(
                    wrapper.getEntityId(),
                    wrapper.getMetadata()
            ));
            wrapper.getMetadata().clean();
        }
    }

}
