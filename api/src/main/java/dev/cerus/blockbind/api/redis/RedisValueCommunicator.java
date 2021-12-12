package dev.cerus.blockbind.api.redis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.blockbind.api.threading.Threading;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RedisValueCommunicator {

    private static final String KEY_BASE = "blockbind:";
    public static final String KEY_ENTITY_ID = KEY_BASE + "entity_id";
    public static final String KEY_SERVERS = KEY_BASE + "servers";
    public static final String KEY_PLAYER_LIST = KEY_BASE + "players";
    public static final String KEY_PLAYER_BASE = KEY_BASE + "player:";

    private final RedisAsyncCommands<String, String> commands;

    public RedisValueCommunicator(final RedisAsyncCommands<String, String> commands) {
        this.commands = commands;
    }

    public void init() {
        this.commands.setnx(KEY_ENTITY_ID, String.valueOf(Integer.MIN_VALUE));
        this.commands.setnx(KEY_SERVERS, "0").whenComplete(($, t) -> this.commands.incr(KEY_SERVERS));
    }

    public CompletableFuture<Void> addPlayer(final PlayerWrapper player) {
        this.commands.multi();
        this.commands.set(KEY_PLAYER_BASE + player.getUuid().toString(), player.encode());
        this.commands.sadd(KEY_PLAYER_LIST, player.getUuid().toString());
        final RedisFuture<TransactionResult> future = this.commands.exec();
        return Threading.whenComplete(future).thenApply($ -> null);
    }

    public CompletableFuture<Void> removePlayer(final PlayerWrapper player) {
        this.commands.multi();
        this.commands.del(KEY_PLAYER_BASE + player.getUuid().toString());
        this.commands.srem(KEY_PLAYER_LIST, player.getUuid().toString());
        final RedisFuture<TransactionResult> future = this.commands.exec();
        return Threading.whenComplete(future).thenApply($ -> null);
    }

    public CompletableFuture<Void> updatePlayer(final PlayerWrapper player) {
        final RedisFuture<String> future = this.commands.set(KEY_PLAYER_BASE + player.getUuid().toString(), player.encode());
        return Threading.whenComplete(future).thenApply($ -> null);
    }

    public CompletableFuture<PlayerWrapper> getPlayer(final UUID uuid) {
        final RedisFuture<String> future = this.commands.get(KEY_PLAYER_BASE + uuid.toString());
        return Threading.whenComplete(future).thenApply(str -> PlayerWrapper.parse(JsonParser.parseString(str).getAsJsonObject()));
    }

    public CompletableFuture<Set<PlayerWrapper>> getPlayers() {
        final Set<PlayerWrapper> players = new HashSet<>();
        final CompletionStage<Set<PlayerWrapper>> future = this.commands.smembers(KEY_PLAYER_LIST)
                .thenCompose(uuidStrings -> {
                    final Set<CompletableFuture<?>> futures = new HashSet<>();
                    for (final String uuidString : uuidStrings) {
                        final CompletableFuture<?> nestedFuture = new CompletableFuture<>();
                        this.commands.get(KEY_PLAYER_BASE + uuidString).whenComplete((rawPlayer, throwable) -> {
                            if (throwable != null) {
                                nestedFuture.completeExceptionally(throwable);
                            } else {
                                final JsonObject playerObj = JsonParser.parseString(rawPlayer).getAsJsonObject();
                                final PlayerWrapper player = PlayerWrapper.parse(playerObj);
                                players.add(player);
                                nestedFuture.complete(null);
                            }
                        });
                        futures.add(nestedFuture);
                    }
                    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
                })
                .thenApply($ -> players);
        return Threading.whenComplete(future);
    }

    public CompletableFuture<Integer> getAndIncrementEntityId() {
        this.commands.multi();
        this.commands.get(KEY_ENTITY_ID);
        this.commands.incr(KEY_ENTITY_ID);
        final CompletionStage<Integer> future = this.commands.exec()
                .thenApply(result -> Integer.parseInt(result.get(0).toString()));
        return Threading.whenComplete(future);
    }

}
