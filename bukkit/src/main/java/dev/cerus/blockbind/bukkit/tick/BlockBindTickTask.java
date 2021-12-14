package dev.cerus.blockbind.bukkit.tick;

import dev.cerus.blockbind.api.identity.Identity;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BlockBindTickTask {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final BlockBindBukkitPlugin plugin;
    private final PacketRedisCommunicator packetCommunicator;
    private final RedisValueCommunicator valueCommunicator;

    // List of tickers
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

    /**
     * Start the ticking
     *
     * @param tickDurationMs The duration between each tick
     */
    public void start(final long tickDurationMs) {
        this.executorService.scheduleAtFixedRate(() -> {
            // Don't tick if we're not available
            if (!Identity.getIdentity().isAvailable()) {
                return;
            }

            for (final Ticker ticker : this.tickers) {
                try {
                    ticker.tick(this.plugin, this.packetCommunicator, this.valueCommunicator);
                } catch (final Exception e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Failed to tick " + ticker.getClass().getSimpleName(), e);
                }
            }
        }, 0, tickDurationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the ticking
     */
    public void stop() {
        this.executorService.shutdown();
    }

}
