package dev.cerus.blockbind.api.redis;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface RedisCommunicator<T> {

    CompletableFuture<Void> send(T t);

    void listen(BiConsumer<T, Throwable> callback);

}
