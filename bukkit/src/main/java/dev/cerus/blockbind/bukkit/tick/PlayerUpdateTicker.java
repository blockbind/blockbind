package dev.cerus.blockbind.bukkit.tick;

import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import org.bukkit.Bukkit;

public class PlayerUpdateTicker implements Ticker {

    private int ticks;

    @Override
    public void tick(final BlockBindBukkitPlugin plugin, final PacketRedisCommunicator packetCommunicator, final RedisValueCommunicator valueCommunicator) {
        if (this.ticks++ % 5 == 0) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (plugin.getUuidPlayerMap().containsKey(player.getUniqueId()) && player.isOnline()) {
                    valueCommunicator.updatePlayer(plugin.getUuidPlayerMap().get(player.getUniqueId()));
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
