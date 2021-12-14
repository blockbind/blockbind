package dev.cerus.blockbind.api.entity;

import java.util.UUID;

/**
 * Represents a complex and dynamic game object
 * <p>
 * All the metadata was implemented using wiki.vg
 */
public abstract class Entity {

    /* Metadata indexes */
    public static final int META_KEY_MASK = 0;
    public static final int META_KEY_AIR = 1;
    public static final int META_KEY_CUSTOM_NAME = 2;
    public static final int META_KEY_CUSTOM_NAME_VISIBLE = 3;
    public static final int META_KEY_SILENT = 4;
    public static final int META_KEY_NO_GRAVITY = 5;
    public static final int META_KEY_POSE = 6;

    protected final UUID uuid;
    protected final int entityId;
    protected final Metadata metadata;
    protected double x;
    protected double y;
    protected double z;
    protected float yaw;
    protected float pitch;

    public Entity(final UUID uuid, final int entityId) {
        this.uuid = uuid;
        this.entityId = entityId;
        this.metadata = new Metadata();
    }

    public Entity(final UUID uuid, final int entityId, final double x, final double y, final double z, final float yaw, final float pitch) {
        this.uuid = uuid;
        this.entityId = entityId;
        this.metadata = new Metadata();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    private byte getBitMask() {
        return (byte) this.metadata.getSafe(0x0, () ->
                new Metadata.Entry(Metadata.EntryType.BYTE, (byte) 0)).getValue();
    }

    private void setBitMask(final byte val) {
        this.metadata.set(0x0, new Metadata.Entry(Metadata.EntryType.BYTE, val));
    }

    public boolean isOnFire() {
        return (this.getBitMask() & 0x01) == 0x01;
    }

    public void setOnFire(final boolean val) {
        this.setBitMask(val ? (byte) (this.getBitMask() | (byte) 0x01) : (byte) (this.getBitMask() & (byte) ~0x01));
    }

    public boolean isCrouching() {
        return (this.getBitMask() & 0x02) == 0x02;
    }

    public void setCrouching(final boolean val) {
        this.setBitMask(val ? (byte) (this.getBitMask() | (byte) 0x02) : (byte) (this.getBitMask() & (byte) ~0x02));
    }

    public boolean isSprinting() {
        return (this.getBitMask() & 0x08) == 0x08;
    }

    public void setSprinting(final boolean val) {
        this.setBitMask(val ? (byte) (this.getBitMask() | (byte) 0x08) : (byte) (this.getBitMask() & (byte) ~0x08));
    }

    public boolean isSwimming() {
        return (this.getBitMask() & 0x10) == 0x10;
    }

    public void setSwimming(final boolean val) {
        this.setBitMask(val ? (byte) (this.getBitMask() | (byte) 0x10) : (byte) (this.getBitMask() & (byte) ~0x10));
    }

    public boolean isInvisible() {
        return (this.getBitMask() & 0x20) == 0x20;
    }

    public void setInvisible(final boolean val) {
        this.setBitMask(val ? (byte) (this.getBitMask() | (byte) 0x20) : (byte) (this.getBitMask() & (byte) ~0x20));
    }

    public boolean isGlowing() {
        return (this.getBitMask() & 0x40) == 0x40;
    }

    public void setGlowing(final boolean val) {
        this.setBitMask(val ? (byte) (this.getBitMask() | (byte) 0x40) : (byte) (this.getBitMask() & (byte) ~0x40));
    }

    public boolean isElytraFlying() {
        return (this.getBitMask() & 0x80) == 0x80;
    }

    public void setElytraFlying(final boolean val) {
        this.setBitMask(val ? (byte) (this.getBitMask() | (byte) 0x80) : (byte) (this.getBitMask() & (byte) ~0x80));
    }

    public int getAirTicks() {
        return (int) this.metadata.getSafe(1, () -> new Metadata.Entry(Metadata.EntryType.INT, 300)).getValue();
    }

    public void setAirTicks(final int val) {
        this.metadata.set(1, new Metadata.Entry(Metadata.EntryType.INT, val));
    }

    public String getCustomName() {
        if (!this.metadata.has(2) || this.metadata.get(2).getValue() == null) {
            return null;
        }
        return (String) this.metadata.getSafe(2, () -> new Metadata.Entry(Metadata.EntryType.OPTIONAL_CHAT, null)).getValue();
    }

    public void setCustomName(final String val) {
        this.metadata.set(2, new Metadata.Entry(Metadata.EntryType.OPTIONAL_CHAT, val));
    }

    public boolean isCustomNameVisible() {
        return (boolean) this.metadata.getSafe(3, () -> new Metadata.Entry(Metadata.EntryType.BOOLEAN, false)).getValue();
    }

    public void setCustomNameVisible(final boolean val) {
        this.metadata.set(3, new Metadata.Entry(Metadata.EntryType.BOOLEAN, val));
    }

    public boolean isSilent() {
        return (boolean) this.metadata.getSafe(4, () -> new Metadata.Entry(Metadata.EntryType.BOOLEAN, false)).getValue();
    }

    public void setSilent(final boolean val) {
        this.metadata.set(4, new Metadata.Entry(Metadata.EntryType.BOOLEAN, val));
    }

    public boolean hasNoGravity() {
        return (boolean) this.metadata.getSafe(5, () -> new Metadata.Entry(Metadata.EntryType.BOOLEAN, false)).getValue();
    }

    public void setNoGravity(final boolean val) {
        this.metadata.set(5, new Metadata.Entry(Metadata.EntryType.BOOLEAN, val));
    }

    public byte getPose() {
        return (byte) this.metadata.getSafe(6, () -> new Metadata.Entry(Metadata.EntryType.POSE, (byte) 0)).getValue();
    }

    public void setPose(final byte val) {
        this.metadata.set(6, new Metadata.Entry(Metadata.EntryType.POSE, val));
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Metadata getMetadata() {
        return this.metadata;
    }

    public double getX() {
        return this.x;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(final double z) {
        this.z = z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }
}
