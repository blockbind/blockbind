package dev.cerus.blockbind.bukkit.listener.server;

import dev.cerus.blockbind.api.packet.entity.EntityMetadataPacket;
import dev.cerus.blockbind.api.packet.player.PlayerInfoPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Bukkit listener for player joining
 */
public class PlayerJoinListener implements Listener {

    private final BlockBindBukkitPlugin plugin;
    private final PacketRedisCommunicator packetCommunicator;
    private final RedisValueCommunicator valueCommunicator;

    public PlayerJoinListener(final BlockBindBukkitPlugin plugin,
                              final PacketRedisCommunicator packetCommunicator,
                              final RedisValueCommunicator valueCommunicator) {
        this.plugin = plugin;
        this.packetCommunicator = packetCommunicator;
        this.valueCommunicator = valueCommunicator;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        for (final PlayerWrapper wrapper : this.plugin.getUuidPlayerMap().values()) {
            this.plugin.getAdapter().sendPlayerInfo(PlayerInfoPacket.addPlayer(wrapper), wrapper);
        }

        // Get next available entity id
        this.valueCommunicator.getAndIncrementEntityId().whenComplete((eid, throwable) -> {
            if (throwable != null) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to fetch entity id", throwable);
                return;
            }

            // Wrap player
            final PlayerWrapper wrapper = new PlayerWrapper(
                    player.getUniqueId(),
                    player.getName(),
                    eid,
                    this.plugin.getAdapter().getPlayerProperties(player.getUniqueId()),
                    this.plugin.getAdapter().getPlayerPropertySignatures(player.getUniqueId()),
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ(),
                    player.getLocation().getYaw(),
                    player.getLocation().getPitch(),
                    player.getGameMode().getValue(),
                    player.getPing(),
                    ComponentSerializer.toString(new TextComponent(player.getDisplayName()))
            );
            wrapper.setSkinMask(this.plugin.getAdapter().getSkinParts(player.getUniqueId()));
            wrapper.setPose((byte) player.getPose().ordinal());

            // Cache and sync wrapper
            this.plugin.getNameUuidMap().put(player.getName(), player.getUniqueId());
            this.plugin.getEntityIdUuidMap().put(eid, player.getUniqueId());
            this.plugin.getUuidPlayerMap().put(player.getUniqueId(), wrapper);
            this.valueCommunicator.addPlayer(wrapper).whenComplete(($, t) -> {
                if (t != null) {
                    this.plugin.getLogger().log(Level.SEVERE, "Failed to sync player", t);
                    return;
                }

                // Send corresponding packets
                this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_PLAYER, PlayerInfoPacket.addPlayer(wrapper));
                this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityMetadataPacket(
                        wrapper.getEntityId(),
                        wrapper.getMetadata()
                ));
                wrapper.getMetadata().clean();
            });
        });
    }

}
