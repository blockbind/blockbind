package dev.cerus.blockbind.bukkit.listener.packet;

import dev.cerus.blockbind.api.packet.Packet;
import dev.cerus.blockbind.api.packet.player.PlayerInfoPacket;
import dev.cerus.blockbind.api.packet.player.SpawnPlayerPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import dev.cerus.blockbind.bukkit.entity.EntityObservers;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerPacketListener implements BiConsumer<Packet, Throwable> {

    private final BlockBindBukkitPlugin plugin;
    private final RedisValueCommunicator valueCommunicator;

    public PlayerPacketListener(final BlockBindBukkitPlugin plugin, final RedisValueCommunicator valueCommunicator) {
        this.plugin = plugin;
        this.valueCommunicator = valueCommunicator;
    }

    @Override
    public void accept(final Packet packet, final Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
            System.err.println("Could not read packet");
            return;
        }

        if (packet instanceof PlayerInfoPacket playerInfoPacket) {
            this.handleInfo(playerInfoPacket);
        } else if (packet instanceof SpawnPlayerPacket spawnPlayerPacket) {
            this.handleSpawnPlayer(spawnPlayerPacket);
        }
    }

    private void handleInfo(final PlayerInfoPacket packet) {
        if (packet.getType() == PlayerInfoPacket.Type.REMOVE_PLAYER) {
            final PlayerWrapper player = this.plugin.getUuidPlayerMap().get(packet.getUuid());
            this.plugin.getLogger().info("Foreign player " + player.getName() + " left");
            this.plugin.getAdapter().sendPlayerInfo(packet, player);
            this.plugin.getNameUuidMap()
                    .remove(this.plugin.getUuidPlayerMap()
                            .remove(packet.getUuid()).getName());
            EntityObservers.clear(player.getEntityId());
            return;
        }

        this.valueCommunicator.getPlayer(packet.getUuid()).whenComplete((player, throwable) -> {
            if (throwable != null) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to read PlayerInfoPacket", throwable);
                return;
            }

            switch (packet.getType()) {
                case ADD_PLAYER -> {
                    this.plugin.getLogger().info("Foreign player " + player.getName() + " joined");
                    this.plugin.getEntityIdUuidMap().put(player.getEntityId(), player.getUuid());
                    this.plugin.getUuidPlayerMap().put(player.getUuid(), player);
                    this.plugin.getNameUuidMap().put(player.getName(), packet.getUuid());
                }
                case UPDATE_GAMEMODE -> {
                    final PlayerWrapper wrapper = this.plugin.getUuidPlayerMap().get(packet.getUuid());
                    wrapper.setGamemode(player.getGamemode());
                }
                case UPDATE_LATENCY -> {
                    final PlayerWrapper wrapper = this.plugin.getUuidPlayerMap().get(packet.getUuid());
                    wrapper.setPing(player.getPing());
                }
                case UPDATE_DISPLAYNAME -> {
                    final PlayerWrapper wrapper = this.plugin.getUuidPlayerMap().get(packet.getUuid());
                    wrapper.setDisplayName(player.getDisplayName());
                }
            }

            this.plugin.getAdapter().sendPlayerInfo(packet, player);
        });
    }

    private void handleSpawnPlayer(final SpawnPlayerPacket packet) {
        if (this.plugin.getEntityIdUuidMap().containsKey(packet.getReceiver())) {
            final UUID uuid = this.plugin.getEntityIdUuidMap().get(packet.getReceiver());
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                this.plugin.getAdapter().spawnPlayer(packet, Set.of(uuid));
            }
        }
    }

}
