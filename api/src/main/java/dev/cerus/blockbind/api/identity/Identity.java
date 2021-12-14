package dev.cerus.blockbind.api.identity;

import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * A simple singleton used for identification purposed and to accomplish basic tasks.
 */
public abstract class Identity {

    private static Identity instance = null;

    /**
     * Gets the identity implementation
     *
     * @return The implementation
     */
    public static Identity getIdentity() {
        return instance;
    }

    /**
     * Sets the identity implementation
     *
     * @param identity The new implementation
     */
    public static void setIdentity(@NotNull final Identity identity) {
        Identity.instance = identity;
    }

    /**
     * Runs a task on the main thread of the application.
     *
     * @param runnable The task to run
     */
    public abstract void runSync(Runnable runnable);

    /**
     * Is this running on the main thread?
     *
     * @return Whether we are on the main thread or not
     */
    public abstract boolean isMainThread();

    /**
     * Is this Block Bind instance available?
     * Should return false if it is currently shutting down
     *
     * @return Whether we are available or not
     */
    public abstract boolean isAvailable();

    /**
     * Gets the name of this identity
     *
     * @return The name
     */
    public abstract String getName();

    /**
     * Gets the Logger of this identity
     *
     * @return The logger
     */
    public abstract Logger getLogger();

}
