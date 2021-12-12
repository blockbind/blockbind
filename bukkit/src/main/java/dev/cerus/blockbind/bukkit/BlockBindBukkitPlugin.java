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

public class BlockBindBukkitPlugin extends JavaPlugin {

    private static final long TICK_DURATION = 50;

    private final Map<UUID, PlayerWrapper> uuidPlayerMap = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameUuidMap = new ConcurrentHashMap<>();
    private final Map<Integer, UUID> entityIdUuidMap = new ConcurrentHashMap<>();

    private final Set<AutoCloseable> closeables = new HashSet<>();
    private RedisClient redisClient;
    private String serverName;
    private PlatformAdapter adapter;

    @Override
    public void onEnable() {
        final boolean firstStart = !new File(this.getDataFolder(), "config.yml").exists();
        this.saveDefaultConfig();
        this.loadConfig();

        if (firstStart) {
            this.getLogger().info("Please edit the Block Bind config and restart your server.");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        this.adapter = PlatformChooser.choose();
        if (this.adapter == null) {
            this.getLogger().severe("Unsupported server version");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        Identity.setIdentity(new BukkitIdentity(this));

        this.redisClient = RedisClient.create(this.getConfig().getString("redis.url"));
        final StatefulRedisPubSubConnection<String, String> pubCon = this.redisClient.connectPubSub();
        final StatefulRedisPubSubConnection<String, String> subCon = this.redisClient.connectPubSub();
        this.closeables.add(pubCon);
        this.closeables.add(subCon);

        final StatefulRedisConnection<String, String> con = this.redisClient.connect();
        final RedisValueCommunicator valueCommunicator = new RedisValueCommunicator(con.async());
        valueCommunicator.init();
        this.closeables.add(con);

        final PacketRedisCommunicator packetCommunicator = new PacketRedisCommunicator(pubCon, subCon);
        packetCommunicator.listen(PacketRedisCommunicator.CHANNEL_PLAYER, new PlayerPacketListener(this, valueCommunicator));
        packetCommunicator.listen(PacketRedisCommunicator.CHANNEL_ENTITY, new EntityPacketListener(this));

        final BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerDependency(PacketRedisCommunicator.class, packetCommunicator);
        commandManager.registerDependency(RedisValueCommunicator.class, valueCommunicator);
        commandManager.registerCommand(new BlockBindCommand());

        final PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this, packetCommunicator, valueCommunicator), this);
        pluginManager.registerEvents(new PlayerQuitListener(this, packetCommunicator, valueCommunicator), this);
        pluginManager.registerEvents(new PlayerMoveListener(this, packetCommunicator), this);

        final BlockBindTickTask tickTask = new BlockBindTickTask(this, packetCommunicator, valueCommunicator);
        tickTask.start(TICK_DURATION);
        this.closeables.add(tickTask::stop);

        this.getLogger().info("Fetching players...");
        valueCommunicator.getPlayers().whenComplete((players, throwable) -> {
            if (throwable != null) {
                this.getLogger().log(Level.SEVERE, "Failed to fetch players", throwable);
                return;
            }

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
        if (this.redisClient != null) {
            this.getLogger().info("Cleaning up...");
            final RedisCommands<String, String> commands = this.redisClient.connect().sync();
            for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                this.getLogger().info("Removing " + onlinePlayer.getName());
                commands.srem(RedisValueCommunicator.KEY_PLAYER_LIST, onlinePlayer.getUniqueId().toString());
            }
            commands.decr(RedisValueCommunicator.KEY_SERVERS);
            if (Integer.parseInt(commands.get(RedisValueCommunicator.KEY_SERVERS)) <= 0) {
                this.getLogger().info("All Block Bind servers have shut down");
                commands.del(RedisValueCommunicator.KEY_ENTITY_ID);
                commands.del(RedisValueCommunicator.KEY_PLAYER_LIST);
            }
            commands.getStatefulConnection().close();
            this.getLogger().info("Cleanup done");
        }

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

}
