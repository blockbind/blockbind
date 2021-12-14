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

public class EntityObservationTicker implements Ticker {

    public static final int PLAYER_RANGE = 10;
    private int ticks;

    @Override
    public void tick(final BlockBindBukkitPlugin plugin, final PacketRedisCommunicator packetCommunicator, final RedisValueCommunicator valueCommunicator) {
        if (this.ticks++ < 10) {
            return;
        }
        this.ticks = 0;

        final Set<PlayerWrapper> players = new HashSet<>(plugin.getUuidPlayerMap().values());
        for (final PlayerWrapper player : players) {
            if (Bukkit.getPlayer(player.getUuid()) == null) {
                continue;
            }

            for (final PlayerWrapper otherPlayer : players) {
                if (player == otherPlayer) {
                    continue;
                }

                final double dist = this.square(player.getX() - otherPlayer.getX())
                        + this.square(player.getY() - otherPlayer.getY())
                        + this.square(player.getZ() - otherPlayer.getZ());
                final boolean inRange = dist <= this.square(PLAYER_RANGE);

                if (inRange && !EntityObservers.isObserving(player.getUuid(), otherPlayer.getEntityId())) {
                    EntityObservers.getObservedEntities(player.getUuid()).add(otherPlayer.getEntityId());
                    /*packetCommunicator.send(PacketRedisCommunicator.CHANNEL_PLAYER, new SpawnPlayerPacket(
                            player.getEntityId(),
                            otherPlayer.getEntityId(),
                            otherPlayer.getUuid(),
                            otherPlayer.getX(),
                            otherPlayer.getY(),
                            otherPlayer.getZ(),
                            otherPlayer.getYaw(),
                            otherPlayer.getPitch()
                    ));*/
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
                    EntityObservers.getObservedEntities(player.getUuid()).remove(otherPlayer.getEntityId());
                    /*packetCommunicator.send(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityDestroyPacket(
                            player.getEntityId(),
                            new int[] {otherPlayer.getEntityId()}
                    ));*/
                    plugin.getAdapter().destroyEntity(new int[] {otherPlayer.getEntityId()}, List.of(player.getUuid()));
                }
            }
        }
    }

    private double square(final double n) {
        return n * n;
    }

}
