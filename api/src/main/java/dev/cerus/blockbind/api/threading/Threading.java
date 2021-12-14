package dev.cerus.blockbind.api.threading;

import dev.cerus.blockbind.api.identity.Identity;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Multithreading utility
 */
public class Threading {

    /**
     * Thread id counter
     */
    private static int threadId = 0;

    /**
     * Thread cache
     */
    private static final ExecutorService exec = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, String.format("BlockBind-Worker-%d", threadId++));
        thread.setUncaughtExceptionHandler((t, e) ->
                Identity.getIdentity().getLogger().log(Level.WARNING, "Uncaught exception in thread " + t.getName(), e));
        return thread;
    });

    private Threading() {
    }

    /**
     * Cease all operations
     */
    public static void shutdown() {
        exec.shutdown();
    }

    /**
     * Offload a task from the main thread
     *
     * @param runnable The task to run async
     */
    public static void runAsync(final Runnable runnable) {
        exec.execute(runnable);
    }

    /**
     * Convenience method for using the "correct" Threading#whenComplete() method based on the current environment
     *
     * @param future The future to wait for
     * @param <T>    Future type
     *
     * @return A new future
     */
    public static <T> CompletableFuture<T> whenComplete(final CompletionStage<T> future) {
        if (Identity.getIdentity().isMainThread()) {
            return whenCompleteSync(future);
        } else {
            return whenCompleteAsync(future);
        }
    }

    /**
     * Returns a new future which will complete synchronously when the provided future completes
     *
     * @param future The future that we will wait for
     * @param <T>    Future type
     *
     * @return A new future
     */
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

    /**
     * Returns a new future which will complete asynchronously when the provided future completes
     *
     * @param future The future that we will wait for
     * @param <T>    Future type
     *
     * @return A new future
     */
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
