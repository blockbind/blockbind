package dev.cerus.blockbind.bukkit.tick;

import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;

/**
 * Represents something that can be ticked
 */
public interface Ticker {

    /**
     * Called by the tick task to tick this object
     *
     * @param plugin             The Block Bind plugin
     * @param packetCommunicator The packet communicator
     * @param valueCommunicator  The value communicator
     */
    void tick(BlockBindBukkitPlugin plugin, PacketRedisCommunicator packetCommunicator, RedisValueCommunicator valueCommunicator);

}
