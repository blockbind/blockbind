package dev.cerus.blockbind.bukkit.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class that is tracking who's observing what
 */
public class EntityObservers {

    private static final Map<UUID, Set<Integer>> playerObserverMap = new ConcurrentHashMap<>();

    private EntityObservers() {
    }

    /**
     * Remove the uuid from the tracked entities map
     *
     * @param uuid The uuid of the observer to remove
     */
    public static void clear(final UUID uuid) {
        playerObserverMap.remove(uuid);
    }

    /**
     * Remove the entity id from the tracked entities map
     *
     * @param eid The id of the entity to remove from all tracking sets
     */
    public static void clear(final int eid) {
        for (final UUID uuid : playerObserverMap.keySet()) {
            playerObserverMap.get(uuid).remove(eid);
        }
    }

    /**
     * Is the owner of the uuid observing the entity with the provided id?
     *
     * @param uuid The uuid of the observer
     * @param eid  The entity id of the potentially tracked entity
     *
     * @return True or false
     */
    public static boolean isObserving(final UUID uuid, final Integer eid) {
        return getObservedEntities(uuid).contains(eid);
    }

    /**
     * Get all entity ids that the owner of the uuid currently observes
     *
     * @param uuid The uuid of the observer
     *
     * @return All entity ids observed by the owner of the uuid
     */
    public static Set<Integer> getObservedEntities(final UUID uuid) {
        return playerObserverMap.computeIfAbsent(uuid, $ -> new HashSet<>());
    }

}
