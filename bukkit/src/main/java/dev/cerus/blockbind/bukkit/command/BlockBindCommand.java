package dev.cerus.blockbind.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import dev.cerus.blockbind.api.redis.PacketRedisCommunicator;
import dev.cerus.blockbind.api.redis.RedisValueCommunicator;
import dev.cerus.blockbind.bukkit.BlockBindBukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@CommandAlias("blockbind")
public class BlockBindCommand extends BaseCommand {

    @Dependency
    private PacketRedisCommunicator packetCommunicator;

    @Dependency
    private RedisValueCommunicator valueCommunicator;

    @Subcommand("testpacket")
    public void handleTestPacket(final CommandSender sender) {
        //this.packetCommunicator.send(PacketRedisCommunicator.CHANNEL_PLAYER, new PlayerInfoPacket(UUID.randomUUID(), "Player", Map.of()));
        sender.sendMessage("PlayerInfoPacket sent");
    }

    @Subcommand("testid")
    public void handleTestId(final CommandSender sender, final boolean async) {
        final Runnable run = () -> {
            final long now = System.currentTimeMillis();
            sender.sendMessage("Please wait...");
            this.valueCommunicator.getAndIncrementEntityId().whenComplete((integer, throwable) -> {
                if (throwable != null) {
                    sender.sendMessage("Error: " + throwable.getMessage());
                    throwable.printStackTrace();
                } else {
                    sender.sendMessage("#" + integer);
                    sender.sendMessage("Took " + (System.currentTimeMillis() - now) + "ms");
                }
            });
        };

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getProvidingPlugin(BlockBindBukkitPlugin.class), run);
        } else {
            Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(BlockBindBukkitPlugin.class), run);
        }
    }

}
