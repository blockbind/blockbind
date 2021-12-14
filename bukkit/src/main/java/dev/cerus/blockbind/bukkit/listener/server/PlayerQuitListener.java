package dev.cerus.blockbind.bukkit.listener.server;

import dev.cerus.blockbind.api.packet.player.PlayerInfoPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import dev.cerus.blockbind.bukkit.entity.EntityObservers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Bukkit listener for player leaving
 */
public class PlayerQuitListener implements Listener {

    private final BlockBindBukkitPlugin plugin;
    private final PacketRedisCommunicator packetCommunicator;
    private final RedisValueCommunicator valueCommunicator;

    public PlayerQuitListener(final BlockBindBukkitPlugin plugin,
                              final PacketRedisCommunicator packetCommunicator,
                              final RedisValueCommunicator valueCommunicator) {
        this.plugin = plugin;
        this.packetCommunicator = packetCommunicator;
        this.valueCommunicator = valueCommunicator;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        // Remove player from sync and cache
        final PlayerWrapper wrapper = this.plugin.getUuidPlayerMap().remove(this.plugin.getNameUuidMap().remove(player.getName()));
        this.plugin.getEntityIdUuidMap().remove(wrapper.getEntityId());
        this.valueCommunicator.removePlayer(wrapper);
        this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_PLAYER, PlayerInfoPacket.removePlayer(wrapper));
        EntityObservers.clear(player.getUniqueId());
        EntityObservers.clear(wrapper.getEntityId());
    }

}
