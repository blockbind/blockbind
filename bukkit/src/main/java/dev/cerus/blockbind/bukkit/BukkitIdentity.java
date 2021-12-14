package dev.cerus.blockbind.bukkit;

import dev.cerus.blockbind.api.identity.Identity;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

/**
 * Bukkit implementation for the Identity interface
 */
public class BukkitIdentity extends Identity {

    private final BlockBindBukkitPlugin plugin;

    public BukkitIdentity(final BlockBindBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runSync(final Runnable runnable) {
        this.plugin.getServer().getScheduler().runTask(this.plugin, runnable);
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public String getName() {
        return this.plugin.getServerName();
    }

    @Override
    public Logger getLogger() {
        return this.plugin.getLogger();
    }

}
