package dev.cerus.blockbind.bukkit.platform;

import dev.cerus.blockbind.api.platform.PlatformAdapter;
import dev.cerus.blockbind.platform.PlatformAdapter16R3;
import dev.cerus.blockbind.platform.PlatformAdapter18R1;
import org.bukkit.Bukkit;

/**
 * Simple utility for choosing the right adapter
 */
public class PlatformChooser {

    private PlatformChooser() {
    }

    /**
     * Attempts to find a matching platform adapter
     *
     * @return A matching adapter or null
     */
    public static PlatformAdapter choose() {
        String version = Bukkit.getVersion();
        version = version.substring(version.indexOf("MC: ") + 4, version.lastIndexOf(')'));

        return switch (version) {
            case "1.16.5" -> new PlatformAdapter16R3();
            case "1.18" -> new PlatformAdapter18R1();
            default -> null;
        };
    }

}
