package dev.cerus.blockbind.api.redis;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Represents a communication interface that can send and receive instances of a specific type
 *
 * @param <T> The type that can be sent and received
 */
public interface RedisCommunicator<T> {

    /**
     * Sends an instance of T over the specified channel
     *
     * @param channel The channel to use
     * @param t       The object instance to send
     *
     * @return A future
     */
    CompletableFuture<Void> send(String channel, T t);

    /**
     * Listens for incoming objects on the specified channel
     *
     * @param channel  The channel to listen for
     * @param callback A callback for incoming objects and/or throwables
     */
    void listen(String channel, BiConsumer<T, Throwable> callback);

}
