package dev.cerus.blockbind.bukkit;

import dev.cerus.blockbind.api.compression.CompressionUtil;
import dev.cerus.blockbind.api.identity.Identity;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.bukkit.listener.packet.PlayerPacketListener;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockBindBukkitPlugin extends JavaPlugin {

    private String serverName;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.loadConfig();

        Identity.setIdentity(new BukkitIdentity(this));

        final RedisClient redisClient = RedisClient.create(this.getConfig().getString("redis.url"));
        final StatefulRedisPubSubConnection<String, String> pubCon = redisClient.connectPubSub();
        final StatefulRedisPubSubConnection<String, String> subCon = redisClient.connectPubSub();

        final PacketRedisCommunicator communicator = new PacketRedisCommunicator(pubCon, subCon);
        communicator.listen(PacketRedisCommunicator.CHANNEL_PLAYER, new PlayerPacketListener());
    }

    private void loadConfig() {
        this.serverName = this.getConfig().getString("server.name");
        CompressionUtil.setZstd(this.getConfig().getBoolean("compression.use-zstd"));
    }

    public String getServerName() {
        return this.serverName;
    }

}
