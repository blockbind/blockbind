package dev.cerus.blockbind.api.entity;

import java.util.UUID;

/**
 * Represents a "living" game object (like players, animals or monsters)
 * <p>
 * All the metadata was implemented using wiki.vg
 */
public abstract class LivingEntity extends Entity {

    public static final int META_KEY_HAND = 8;
    public static final int META_KEY_HEALTH = 9;

    public LivingEntity(final UUID uuid, final int entityId) {
        super(uuid, entityId);
    }

    public LivingEntity(final UUID uuid, final int entityId, final double x, final double y, final double z, final float yaw, final float pitch) {
        super(uuid, entityId, x, y, z, yaw, pitch);
    }

    private byte getHandMask() {
        return (byte) this.metadata.getSafe(8, () ->
                new Metadata.Entry(Metadata.EntryType.BYTE, (byte) 0)).getValue();
    }

    private void setHandMask(final byte val) {
        this.metadata.set(8, new Metadata.Entry(Metadata.EntryType.BYTE, val));
    }

    public boolean isHandActive() {
        return (this.getHandMask() & 0x01) == 0x01;
    }

    public void setHandActive(final boolean val) {
        this.setHandMask(val ? (byte) (this.getHandMask() | (byte) 0x01) : (byte) (this.getHandMask() & (byte) ~0x01));
    }

    public byte getActiveHand() {
        return (this.getHandMask() & 0x02) == 0x02 ? (byte) 1 : (byte) 0;
    }

    public void setActiveHand(final byte b) {
        if (b != 1 && b != 0) {
            return;
        }
        final boolean val = b == 1;
        this.setHandMask(val ? (byte) (this.getHandMask() | (byte) 0x02) : (byte) (this.getHandMask() & (byte) ~0x02));
    }

    public boolean isSpinning() {
        return (this.getHandMask() & 0x04) == 0x04;
    }

    public void setSpinning(final boolean val) {
        this.setHandMask(val ? (byte) (this.getHandMask() | (byte) 0x04) : (byte) (this.getHandMask() & (byte) ~0x04));
    }

    public float getHealth() {
        return (float) this.metadata.getSafe(9, () -> new Metadata.Entry(Metadata.EntryType.FLOAT, 1)).getValue();
    }

    public void setHealth(final float val) {
        this.metadata.set(0, new Metadata.Entry(Metadata.EntryType.FLOAT, val));
    }

}
