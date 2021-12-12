package dev.cerus.blockbind.bukkit.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityObservers {

    private static final Map<UUID, Set<Integer>> playerObserverMap = new ConcurrentHashMap<>();

    private EntityObservers() {
    }

    public static void clear(final UUID uuid) {
        playerObserverMap.remove(uuid);
    }

    public static void clear(final int eid) {
        for (final UUID uuid : playerObserverMap.keySet()) {
            playerObserverMap.get(uuid).remove(eid);
        }
    }

    public static boolean isObserving(final UUID uuid, final Integer eid) {
        return getObservedEntities(uuid).contains(eid);
    }

    public static Set<Integer> getObservedEntities(final UUID uuid) {
        return playerObserverMap.computeIfAbsent(uuid, $ -> new HashSet<>());
    }

}
