package dev.cerus.blockbind.bukkit.tick;

import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;

public interface Ticker {

    void tick(BlockBindBukkitPlugin plugin, PacketRedisCommunicator packetCommunicator, RedisValueCommunicator valueCommunicator);

}
