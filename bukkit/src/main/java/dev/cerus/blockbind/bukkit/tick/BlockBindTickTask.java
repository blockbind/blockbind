package dev.cerus.blockbind.bukkit.tick;

import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BlockBindTickTask {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final BlockBindBukkitPlugin plugin;
    private final PacketRedisCommunicator packetCommunicator;
    private final RedisValueCommunicator valueCommunicator;

    private final List<Ticker> tickers = Arrays.asList(
            new PlayerUpdateTicker(),
            new EntityObservationTicker()
    );

    public BlockBindTickTask(final BlockBindBukkitPlugin plugin,
                             final PacketRedisCommunicator packetCommunicator,
                             final RedisValueCommunicator valueCommunicator) {
        this.plugin = plugin;
        this.packetCommunicator = packetCommunicator;
        this.valueCommunicator = valueCommunicator;
    }

    public void start(final long tickDurationMs) {
        this.executorService.scheduleAtFixedRate(() -> {
            for (final Ticker ticker : this.tickers) {
                ticker.tick(this.plugin, this.packetCommunicator, this.valueCommunicator);
            }
        }, 0, tickDurationMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        this.executorService.shutdown();
    }

}
