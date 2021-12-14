package dev.cerus.blockbind.bukkit.tick;

import dev.cerus.blockbind.api.packet.player.SpawnPlayerPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import dev.cerus.blockbind.bukkit.entity.EntityObservers;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;

/**
 * Controls entity observations
 * TODO: Also track other entities besides players
 */
public class EntityObservationTicker implements Ticker {

    /**
     * Tracking range for players
     */
    public static final int PLAYER_RANGE = 40;

    private int ticks;

    @Override
    public void tick(final BlockBindBukkitPlugin plugin, final PacketRedisCommunicator packetCommunicator, final RedisValueCommunicator valueCommunicator) {
        // Only tick every 10 ticks
        if (this.ticks++ < 10) {
            return;
        }
        this.ticks = 0;

        final Set<PlayerWrapper> players = new HashSet<>(plugin.getUuidPlayerMap().values());
        for (final PlayerWrapper player : players) {
            // Check if synced player originates from this server
            if (Bukkit.getPlayer(player.getUuid()) == null) {
                continue;
            }

            for (final PlayerWrapper otherPlayer : players) {
                // Check if these are the same players
                if (player == otherPlayer) {
                    continue;
                }

                // Calculate distance
                final double dist = this.square(player.getX() - otherPlayer.getX())
                        + this.square(player.getY() - otherPlayer.getY())
                        + this.square(player.getZ() - otherPlayer.getZ());
                final boolean inRange = dist <= this.square(PLAYER_RANGE);

                if (inRange && !EntityObservers.isObserving(player.getUuid(), otherPlayer.getEntityId())) {
                    // Other player is in range; observe
                    EntityObservers.getObservedEntities(player.getUuid()).add(otherPlayer.getEntityId());
                    plugin.getAdapter().spawnPlayer(new SpawnPlayerPacket(
                            player.getEntityId(),
                            otherPlayer.getEntityId(),
                            otherPlayer.getUuid(),
                            otherPlayer.getX(),
                            otherPlayer.getY(),
                            otherPlayer.getZ(),
                            otherPlayer.getYaw(),
                            otherPlayer.getPitch()
                    ), List.of(player.getUuid()));
                    plugin.getAdapter().sendHeadRot(otherPlayer, List.of(player.getUuid()));
                    plugin.getAdapter().sendMetadata(otherPlayer.getEntityId(), otherPlayer.getMetadata(), List.of(player.getUuid()));
                } else if (!inRange && EntityObservers.isObserving(player.getUuid(), otherPlayer.getEntityId())) {
                    // Other player is out of range; don't observe anymore
                    EntityObservers.getObservedEntities(player.getUuid()).remove(otherPlayer.getEntityId());
                    plugin.getAdapter().destroyEntity(new int[] {otherPlayer.getEntityId()}, List.of(player.getUuid()));
                }
            }
        }
    }

    private double square(final double n) {
        return n * n;
    }

}
