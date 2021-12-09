package dev.cerus.blockbind.bukkit;

import dev.cerus.blockbind.api.identity.Identity;

public class BukkitIdentity extends Identity {

    private final BlockBindBukkitPlugin plugin;

    public BukkitIdentity(final BlockBindBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return this.plugin.getServerName();
    }

}
