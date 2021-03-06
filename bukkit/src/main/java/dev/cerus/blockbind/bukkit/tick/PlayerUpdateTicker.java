package dev.cerus.blockbind.bukkit.tick;

import dev.cerus.blockbind.api.packet.entity.EntityMetadataPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import org.bukkit.Bukkit;

/**
 * Syncs local player data with Redis and pulls updated data from other servers from Redis
 */
public class PlayerUpdateTicker implements Ticker {

    private int ticks;

    @Override
    public void tick(final BlockBindBukkitPlugin plugin, final PacketRedisCommunicator packetCommunicator, final RedisValueCommunicator valueCommunicator) {
        if (this.ticks++ % 5 == 0) {
            // Run every 5 ticks
            Bukkit.getOnlinePlayers().forEach(player -> {
                // Update player if online
                if (plugin.getUuidPlayerMap().containsKey(player.getUniqueId()) && player.isOnline()) {
                    // Update
                    final PlayerWrapper wrapper = plugin.getUuidPlayerMap().get(player.getUniqueId());
                    valueCommunicator.updatePlayer(wrapper);

                    // Update skin data if necessary
                    final byte skinParts = plugin.getAdapter().getSkinParts(player.getUniqueId());
                    if (skinParts != wrapper.getSkinMask()) {
                        wrapper.setSkinMask(skinParts);
                    }
                    // Update pose if necessary
                    if (player.getPose().ordinal() != wrapper.getPose()) {
                        wrapper.setPose((byte) player.getPose().ordinal());
                    }

                    // Broadcast meta packet if necessary
                    if (wrapper.getMetadata().isDirty()) {
                        packetCommunicator.send(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityMetadataPacket(
                                wrapper.getEntityId(),
                                wrapper.getMetadata()
                        ));
                        wrapper.getMetadata().clean();
                    }
                }
            });
        }
        if (this.ticks % 20 == 0) {
            valueCommunicator.getPlayers().whenComplete((players, throwable) -> {
                for (final PlayerWrapper player : players) {
                    if (Bukkit.getPlayer(player.getUuid()) == null
                            && plugin.getUuidPlayerMap().containsKey(player.getUuid())) {
                        final PlayerWrapper local = plugin.getUuidPlayerMap().get(player.getUuid());
                        local.overwrite(player);
                    }
                }
            });
            this.ticks = 0;
        }
    }

}
