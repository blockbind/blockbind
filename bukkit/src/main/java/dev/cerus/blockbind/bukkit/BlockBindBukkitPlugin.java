package dev.cerus.blockbind.bukkit;

import co.aikar.commands.BukkitCommandManager;
import dev.cerus.blockbind.api.compression.CompressionUtil;
import dev.cerus.blockbind.api.identity.Identity;
import dev.cerus.blockbind.api.platform.PlatformAdapter;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.api.threading.Threading;
import dev.cerus.blockbind.bukkit.command.BlockBindCommand;
import dev.cerus.blockbind.bukkit.listener.packet.EntityPacketListener;
import dev.cerus.blockbind.bukkit.listener.packet.PlayerPacketListener;
import dev.cerus.blockbind.bukkit.listener.server.PlayerCrouchListener;
import dev.cerus.blockbind.bukkit.listener.server.PlayerElytraListener;
import dev.cerus.blockbind.bukkit.listener.server.PlayerJoinListener;
import dev.cerus.blockbind.bukkit.listener.server.PlayerMoveListener;
import dev.cerus.blockbind.bukkit.listener.server.PlayerQuitListener;
import dev.cerus.blockbind.bukkit.platform.PlatformChooser;
import dev.cerus.blockbind.bukkit.tick.BlockBindTickTask;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bootstrapper of the Bukkit implementation for Block Bind
 */
public class BlockBindBukkitPlugin extends JavaPlugin {

    /**
     * Time between each tick
     */
    private static final long TICK_DURATION = 50;

    /**
     * Cached players
     * TODO: Move to a dedicated class
     */
    private final Map<UUID, PlayerWrapper> uuidPlayerMap = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameUuidMap = new ConcurrentHashMap<>();
    private final Map<Integer, UUID> entityIdUuidMap = new ConcurrentHashMap<>();

    private final Set<AutoCloseable> closeables = new HashSet<>();
    private RedisClient redisClient;
    private String serverName;
    private PlatformAdapter adapter;
    private boolean shuttingDown;

    @Override
    public void onLoad() {
        this.shuttingDown = false;
    }

    @Override
    public void onEnable() {
        // Save and load config
        final boolean firstStart = !new File(this.getDataFolder(), "config.yml").exists();
        this.saveDefaultConfig();
        this.loadConfig();

        // First start stuff
        if (firstStart) {
            this.getLogger().info("Looks like this is the first server start with Block Bind installed!");
            this.getLogger().info("Please edit the Block Bind config and restart your server.");
            this.getPluginLoader().disablePlugin(this);
            this.shuttingDown = true;
            return;
        }

        // Attempt to find a matching platform adapter
        this.adapter = PlatformChooser.choose();
        if (this.adapter == null) {
            this.getLogger().severe("Unsupported server version");
            this.getPluginLoader().disablePlugin(this);
            this.shuttingDown = true;
            return;
        }

        // Let#s identify ourselves before we do anything
        Identity.setIdentity(new BukkitIdentity(this));

        // Initialize redis
        this.redisClient = RedisClient.create(this.getConfig().getString("redis.url"));
        final StatefulRedisPubSubConnection<String, String> packetPubCon = this.redisClient.connectPubSub();
        final StatefulRedisPubSubConnection<String, String> packetSubCon = this.redisClient.connectPubSub();
        final StatefulRedisConnection<String, String> valueCon = this.redisClient.connect();
        this.closeables.add(packetPubCon);
        this.closeables.add(packetSubCon);
        this.closeables.add(valueCon);

        // Initialize our value communicator
        final RedisValueCommunicator valueCommunicator = new RedisValueCommunicator(valueCon.async());
        valueCommunicator.init();

        // Initialize our packet communicator
        final PacketRedisCommunicator packetCommunicator = new PacketRedisCommunicator(packetPubCon, packetSubCon);
        packetCommunicator.listen(PacketRedisCommunicator.CHANNEL_PLAYER, new PlayerPacketListener(this, valueCommunicator));
        packetCommunicator.listen(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityPacketListener(this));

        // Register commands
        final BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerDependency(PacketRedisCommunicator.class, packetCommunicator);
        commandManager.registerDependency(RedisValueCommunicator.class, valueCommunicator);
        commandManager.registerCommand(new BlockBindCommand());

        // Register event listeners
        final PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this, packetCommunicator, valueCommunicator), this);
        pluginManager.registerEvents(new PlayerQuitListener(this, packetCommunicator, valueCommunicator), this);
        pluginManager.registerEvents(new PlayerMoveListener(this, packetCommunicator), this);
        pluginManager.registerEvents(new PlayerCrouchListener(this, packetCommunicator), this);
        pluginManager.registerEvents(new PlayerElytraListener(this, packetCommunicator), this);

        // Start ticking
        final BlockBindTickTask tickTask = new BlockBindTickTask(this, packetCommunicator, valueCommunicator);
        tickTask.start(TICK_DURATION);
        this.closeables.add(tickTask::stop);

        // Cache all synced players
        this.getLogger().info("Fetching players...");
        valueCommunicator.getPlayers().whenComplete((players, throwable) -> {
            if (throwable != null) {
                this.getLogger().log(Level.SEVERE, "Failed to fetch players", throwable);
                return;
            }

            // Cache each player
            players.forEach(player -> {
                this.entityIdUuidMap.put(player.getEntityId(), player.getUuid());
                this.uuidPlayerMap.put(player.getUuid(), player);
                this.nameUuidMap.put(player.getName(), player.getUuid());
            });
            this.getLogger().info(String.format("Fetched %d players", players.size()));
        });
    }

    @Override
    public void onDisable() {
        this.shuttingDown = true;

        // Perform redis cleanup
        if (this.redisClient != null) {
            this.getLogger().info("Cleaning up...");
            final RedisCommands<String, String> commands = this.redisClient.connect().sync();
            for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                this.getLogger().info("Removing " + onlinePlayer.getName());
                commands.srem(RedisValueCommunicator.KEY_PLAYER_LIST, onlinePlayer.getUniqueId().toString());
            }
            commands.decr(RedisValueCommunicator.KEY_SERVERS);
            if (Integer.parseInt(commands.get(RedisValueCommunicator.KEY_SERVERS)) <= 0) {
                // Delete things like the synced entity id if we are the last instance to shut down
                this.getLogger().info("All Block Bind servers have shut down");
                commands.del(RedisValueCommunicator.KEY_ENTITY_ID);
                commands.del(RedisValueCommunicator.KEY_PLAYER_LIST);
            }
            commands.getStatefulConnection().close();
            this.getLogger().info("Cleanup done");
        }

        // Shut down our threading util and close registered closeables
        Threading.shutdown();
        for (final AutoCloseable closeable : this.closeables) {
            try {
                closeable.close();
            } catch (final Exception e) {
                e.printStackTrace();
                this.getLogger().severe("Failed to close a closable");
            }
        }
    }

    private void loadConfig() {
        this.serverName = this.getConfig().getString("server.name");
        CompressionUtil.setZstd(this.getConfig().getBoolean("compression.use-zstd"));
    }

    public Map<UUID, PlayerWrapper> getUuidPlayerMap() {
        return this.uuidPlayerMap;
    }

    public Map<String, UUID> getNameUuidMap() {
        return this.nameUuidMap;
    }

    public Map<Integer, UUID> getEntityIdUuidMap() {
        return this.entityIdUuidMap;
    }

    public String getServerName() {
        return this.serverName;
    }

    public PlatformAdapter getAdapter() {
        return this.adapter;
    }

    boolean isShuttingDown() {
        return this.shuttingDown;
    }

}
