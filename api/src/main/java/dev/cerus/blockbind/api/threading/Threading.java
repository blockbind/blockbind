package dev.cerus.blockbind.api.threading;

import dev.cerus.blockbind.api.identity.Identity;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class Threading {

    private static int threadId = 0;
    private static final ExecutorService exec = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, String.format("BlockBind-Worker-%d", threadId++));
        thread.setUncaughtExceptionHandler((t, e) ->
                Identity.getIdentity().getLogger().log(Level.WARNING, "Uncaught exception in thread " + t.getName(), e));
        return thread;
    });

    private Threading() {
    }

    public static void shutdown() {
        exec.shutdown();
    }

    public static void runAsync(final Runnable runnable) {
        exec.execute(runnable);
    }

    public static <T> CompletableFuture<T> whenComplete(final CompletionStage<T> future) {
        if (Identity.getIdentity().isMainThread()) {
            return whenCompleteSync(future);
        } else {
            return whenCompleteAsync(future);
        }
    }

    public static <T> CompletableFuture<T> whenCompleteSync(final CompletionStage<T> future) {
        final CompletableFuture<T> fut = new CompletableFuture<>();
        future.whenComplete((t, throwable) ->
                Identity.getIdentity().runSync(() -> {
                    if (throwable != null) {
                        fut.completeExceptionally(throwable);
                    } else {
                        fut.complete(t);
                    }
                }));
        return fut;
    }

    public static <T> CompletableFuture<T> whenCompleteAsync(final CompletionStage<T> future) {
        final CompletableFuture<T> fut = new CompletableFuture<>();
        future.whenComplete((t, throwable) ->
                exec.execute(() -> {
                    if (throwable != null) {
                        fut.completeExceptionally(throwable);
                    } else {
                        fut.complete(t);
                    }
                }));
        return fut;
    }

}
