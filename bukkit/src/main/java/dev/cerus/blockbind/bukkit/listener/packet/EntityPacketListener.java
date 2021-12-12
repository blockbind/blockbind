package dev.cerus.blockbind.bukkit.listener.packet;

import dev.cerus.blockbind.api.packet.Packet;
import dev.cerus.blockbind.api.packet.entity.EntityDestroyPacket;
import dev.cerus.blockbind.api.packet.entity.EntityMovePacket;
import dev.cerus.blockbind.api.packet.entity.EntityMoveRotPacket;
import dev.cerus.blockbind.api.packet.entity.EntityRotPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import dev.cerus.blockbind.bukkit.entity.EntityObservers;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

public class EntityPacketListener implements BiConsumer<Packet, Throwable> {

    private final BlockBindBukkitPlugin plugin;

    public EntityPacketListener(final BlockBindBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(final Packet packet, final Throwable throwable) {
        if (packet instanceof EntityMovePacket relMovePacket) {
            this.handleRelMove(relMovePacket);
        } else if (packet instanceof EntityMoveRotPacket relMoveRotPacket) {
            this.handleRelMoveRot(relMoveRotPacket);
        } else if (packet instanceof EntityRotPacket rotPacket) {
            this.handleRot(rotPacket);
        } else if (packet instanceof EntityDestroyPacket destroyPacket) {
            Bukkit.broadcastMessage("received destroy");
            this.handleDestroy(destroyPacket);
        }
    }

    private void handleRelMove(final EntityMovePacket relMovePacket) {
        final PlayerWrapper player = this.getPlayer(relMovePacket.getEntityId());
        if (player != null) {
            this.plugin.getAdapter().sendRelMove(relMovePacket, this.getObserving(player.getEntityId()));
            player.setX(relMovePacket.getNewX());
            player.setY(relMovePacket.getNewY());
            player.setZ(relMovePacket.getNewZ());
        }
    }

    private void handleRelMoveRot(final EntityMoveRotPacket relMoveRotPacket) {
        final PlayerWrapper player = this.getPlayer(relMoveRotPacket.getEntityId());
        if (player != null) {
            this.plugin.getAdapter().sendRelMoveRot(relMoveRotPacket, this.getObserving(player.getEntityId()));
            player.setX(relMoveRotPacket.getNewX());
            player.setY(relMoveRotPacket.getNewY());
            player.setZ(relMoveRotPacket.getNewZ());
            player.setYaw(relMoveRotPacket.getYaw());
            player.setPitch(relMoveRotPacket.getPitch());

            // Needed for correct head rotation
            this.plugin.getAdapter().sendHeadRot(player, this.getObserving(player.getEntityId()));
        }
    }

    private void handleRot(final EntityRotPacket rotPacket) {
        final PlayerWrapper player = this.getPlayer(rotPacket.getEntityId());
        if (player != null) {
            this.plugin.getAdapter().sendRot(rotPacket, player, this.getObserving(player.getEntityId()));
            player.setYaw(rotPacket.getYaw());
            player.setPitch(rotPacket.getPitch());

            // Needed for correct head rotation
            this.plugin.getAdapter().sendHeadRot(player, this.getObserving(player.getEntityId()));
        }
    }

    private void handleDestroy(final EntityDestroyPacket destroyPacket) {
        final PlayerWrapper player = this.getOnlinePlayer(destroyPacket.getReceiver());
        if (player != null) {
            this.plugin.getAdapter().destroyEntity(destroyPacket.getIds(), List.of(player.getUuid()));
        }
    }

    private Set<UUID> getObserving(final int eid) {
        return this.plugin.getUuidPlayerMap().keySet().stream()
                .filter(uuid -> EntityObservers.isObserving(uuid, eid))
                .collect(Collectors.toSet());
    }

    private PlayerWrapper getOnlinePlayer(final int eid) {
        final PlayerWrapper player = this.getPlayer(eid);
        if (player == null) {
            return null;
        }
        if (Bukkit.getPlayer(player.getUuid()) == null) {
            return null;
        }
        return player;
    }

    private PlayerWrapper getPlayer(final int eid) {
        if (this.plugin.getEntityIdUuidMap().containsKey(eid)) {
            return this.plugin.getUuidPlayerMap().get(this.plugin.getEntityIdUuidMap().get(eid));
        }
        return null;
    }

}
