package dev.cerus.blockbind.api.redis;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface RedisCommunicator<T> {

    CompletableFuture<Void> send(String channel, T t);

    void listen(String channel, BiConsumer<T, Throwable> callback);

}
