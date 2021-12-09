package dev.cerus.blockbind.api.redis;

import dev.cerus.blockbind.api.compression.CompressionUtil;
import dev.cerus.blockbind.api.identity.Identity;
import dev.cerus.blockbind.api.packet.Packet;
import dev.cerus.blockbind.api.packet.PacketRegistry;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class PacketRedisCommunicator extends RedisPubSubAdapter<String, String> implements RedisCommunicator<Packet> {

    public static final String CHANNEL_PLAYER = "blockbind:player";

    private final RedisPubSubAsyncCommands<String, String> pubCmd;
    private final RedisPubSubAsyncCommands<String, String> subCmd;
    private final Map<String, Set<BiConsumer<Packet, Throwable>>> channelSubscribers;

    public PacketRedisCommunicator(final StatefulRedisPubSubConnection<String, String> pubCon,
                                   final StatefulRedisPubSubConnection<String, String> subCon) {
        if (Identity.getIdentity() == null) {
            throw new IllegalStateException("Identity not set");
        }

        this.pubCmd = pubCon.async();
        subCon.addListener(this);
        this.subCmd = subCon.async();
        this.channelSubscribers = new HashMap<>();
    }

    @Override
    public CompletableFuture<Void> send(final String channel, final Packet packet) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            final ByteBuf buf = Unpooled.buffer();
            packet.write(buf);
            final byte[] array = buf.array();
            final byte[] compressed = CompressionUtil.compress(array);
            final String b64Encode = Base64.getEncoder().encodeToString(compressed);
            final String str = Identity.getIdentity().getName() + ":" + array.length + ":" + b64Encode;
            buf.release();

            this.pubCmd.publish(channel, str).whenComplete((aLong, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                } else {
                    future.complete(null);
                }
            });
        } catch (final IOException | IndexOutOfBoundsException e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public void listen(final String channel, final BiConsumer<Packet, Throwable> callback) {
        final Set<BiConsumer<Packet, Throwable>> subs = this.channelSubscribers
                .computeIfAbsent(channel, $ -> new HashSet<>());
        subs.add(callback);
    }

    @Override
    public void message(final String channel, final String message) {
        final Set<BiConsumer<Packet, Throwable>> subs = this.channelSubscribers.get(channel);
        if (subs != null && !subs.isEmpty()) {
            final String[] split = message.split(":", 3);
            final String srv = split[0];
            if (srv.equals(Identity.getIdentity().getName())) {
                return;
            }

            final int len = Integer.parseInt(split[1]);
            final byte[] b64Decode = Base64.getDecoder().decode(split[2].getBytes(StandardCharsets.UTF_8));
            final byte[] decompressed;
            try {
                decompressed = CompressionUtil.decompress(b64Decode, len);
            } catch (final IOException e) {
                subs.forEach(listener -> listener.accept(null, e));
                return;
            }

            try {
                final ByteBuf buf = Unpooled.buffer(decompressed.length, decompressed.length);
                final int id = buf.readInt();
                final Packet packet = PacketRegistry.getPacketById(id);
                packet.read(buf);
                buf.release();
                subs.forEach(listener -> listener.accept(packet, null));
            } catch (final NullPointerException | IndexOutOfBoundsException e) {
                subs.forEach(listener -> listener.accept(null, e));
            }
        }
    }

}
