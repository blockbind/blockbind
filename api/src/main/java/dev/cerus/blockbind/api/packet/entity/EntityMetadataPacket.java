package dev.cerus.blockbind.api.packet.entity;

import dev.cerus.blockbind.api.entity.Metadata;
import dev.cerus.blockbind.api.packet.Packet;
import io.netty.buffer.ByteBuf;

public class EntityMetadataPacket extends Packet {

    private int entityId;
    private Metadata metadata;

    public EntityMetadataPacket(final int entityId, final Metadata metadata) {
        this.entityId = entityId;
        this.metadata = metadata;
    }

    public EntityMetadataPacket() {
    }

    @Override
    public void write(final ByteBuf buffer) {
        buffer.writeInt(this.entityId);
        this.metadata.write(buffer);
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.metadata = new Metadata();
        this.metadata.read(buffer);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Metadata getMetadata() {
        return this.metadata;
    }

}
