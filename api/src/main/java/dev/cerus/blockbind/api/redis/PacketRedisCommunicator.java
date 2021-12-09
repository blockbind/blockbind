package dev.cerus.blockbind.api.redis;

import dev.cerus.blockbind.api.compression.CompressionUtil;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class PacketRedisCommunicator extends RedisPubSubAdapter<String, String> implements RedisCommunicator<Packet> {

    private final RedisPubSubAsyncCommands<String, String> pubCmd;
    private final String channel;
    private BiConsumer<Packet, Throwable> listener;

    public PacketRedisCommunicator(final StatefulRedisPubSubConnection<String, String> pubCon,
                                   final StatefulRedisPubSubConnection<String, String> subCon,
                                   final String channel) {
        this.pubCmd = pubCon.async();
        subCon.addListener(this);
        subCon.async().subscribe(channel);
        this.channel = channel;

        this.listener = (packet, throwable) -> {
            System.err.printf("Failed to read packet: %s%n", throwable.getMessage());
            throwable.printStackTrace();
        };
    }

    @Override
    public CompletableFuture<Void> send(final Packet packet) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            final ByteBuf buf = Unpooled.buffer();
            packet.write(buf);
            final byte[] array = buf.array();
            final byte[] compressed = CompressionUtil.compress(array);
            final String b64Encode = Base64.getEncoder().encodeToString(compressed);
            final String str = array.length + ":" + b64Encode;
            buf.release();
            this.pubCmd.publish(this.channel, str).whenComplete((aLong, throwable) -> {
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
    public void listen(final BiConsumer<Packet, Throwable> callback) {
        this.listener = callback;
    }

    @Override
    public void message(final String channel, final String message) {
        if (this.listener != null) {
            final String[] split = message.split(":", 2);
            final int len = Integer.parseInt(split[0]);
            final byte[] b64Decode = Base64.getDecoder().decode(split[1].getBytes(StandardCharsets.UTF_8));
            final byte[] decompressed;
            try {
                decompressed = CompressionUtil.decompress(b64Decode, len);
            } catch (final IOException e) {
                this.listener.accept(null, e);
                return;
            }

            try {
                final ByteBuf buf = Unpooled.buffer(decompressed.length, decompressed.length);
                final int id = buf.readInt();
                final Packet packet = PacketRegistry.getPacketById(id);
                packet.read(buf);
                buf.release();
                this.listener.accept(packet, null);
            } catch (final NullPointerException | IndexOutOfBoundsException e) {
                this.listener.accept(null, e);
            }
        }
    }

}
