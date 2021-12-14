package dev.cerus.blockbind.api.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import dev.cerus.blockbind.api.entity.LivingEntity;
import dev.cerus.blockbind.api.entity.Metadata;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a connected player
 */
public class PlayerWrapper extends LivingEntity {

    /* Metadata indexes */
    public static final int META_KEY_SKIN = 17;

    private static final Gson GSON = new GsonBuilder()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create();

    private final String name;
    private final Map<String, String> properties;
    private final Map<String, String> propertySignatures;
    private int gamemode;
    private int ping;
    private String displayName;

    public PlayerWrapper(final UUID uuid,
                         final String name,
                         final int entityId,
                         final Map<String, String> properties,
                         final Map<String, String> propertySignatures) {
        super(uuid, entityId);
        this.name = name;
        this.properties = properties;
        this.propertySignatures = propertySignatures;
    }

    public PlayerWrapper(final UUID uuid,
                         final String name,
                         final int entityId,
                         final Map<String, String> properties,
                         final Map<String, String> propertySignatures,
                         final double x,
                         final double y,
                         final double z,
                         final float yaw,
                         final float pitch,
                         final int gamemode,
                         final int ping,
                         final String displayName) {
        super(uuid, entityId);
        this.name = name;
        this.properties = properties;
        this.propertySignatures = propertySignatures;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.gamemode = gamemode;
        this.ping = ping;
        this.displayName = displayName;
    }

    /**
     * Attempts to parse a player wrapper from the provided json structure
     *
     * @param playerObj The json structure to parse from
     *
     * @return The parsed player
     */
    public static PlayerWrapper parse(final JsonObject playerObj) {
        final JsonObject uuidObj = playerObj.get("uuid").getAsJsonObject();
        final JsonObject propertiesObj = playerObj.get("properties").getAsJsonObject();
        final JsonObject sigsObj = playerObj.get("signatures").getAsJsonObject();
        final JsonObject posObj = playerObj.get("pos").getAsJsonObject();

        // Property stuff
        final Map<String, String> properties = new HashMap<>();
        final Map<String, String> signatures = new HashMap<>();
        for (final String key : propertiesObj.keySet()) {
            properties.put(key, propertiesObj.get(key).getAsString());
        }
        for (final String key : sigsObj.keySet()) {
            signatures.put(key, sigsObj.get(key).getAsString());
        }

        // Create instance
        final PlayerWrapper player = new PlayerWrapper(
                new UUID(
                        uuidObj.get("msb").getAsLong(),
                        uuidObj.get("lsb").getAsLong()
                ),
                playerObj.get("name").getAsString(),
                playerObj.get("entity_id").getAsInt(),
                properties,
                signatures,
                posObj.get("x").getAsDouble(),
                posObj.get("y").getAsDouble(),
                posObj.get("z").getAsDouble(),
                posObj.get("yaw").getAsFloat(),
                posObj.get("pitch").getAsFloat(),
                playerObj.get("gamemode").getAsInt(),
                playerObj.get("ping").getAsInt(),
                playerObj.has("display_name") ? playerObj.get("display_name").getAsString() : null
        );

        // Check for metadata; read if present
        if (playerObj.has("meta")) {
            final String metaStr = playerObj.get("meta").getAsString();
            final byte[] metaBytes = Base64.getDecoder().decode(metaStr.getBytes(StandardCharsets.UTF_8));
            final ByteBuf byteBuf = Unpooled.wrappedBuffer(metaBytes);
            player.metadata.read(byteBuf);
            byteBuf.release();
        }

        return player;
    }

    /**
     * Attempts to encode this player wrapper into a json structure
     *
     * @return A json string
     */
    public String encode() {
        final JsonObject object = new JsonObject();
        final JsonObject uuidObj = new JsonObject();
        final JsonObject propertiesObj = new JsonObject();
        final JsonObject sigsObj = new JsonObject();
        final JsonObject posObj = new JsonObject();

        uuidObj.addProperty("msb", this.uuid.getMostSignificantBits());
        uuidObj.addProperty("lsb", this.uuid.getLeastSignificantBits());
        object.add("uuid", uuidObj);
        object.addProperty("name", this.name);
        object.addProperty("entity_id", this.entityId);

        // Property stuff
        this.properties.forEach(propertiesObj::addProperty);
        this.propertySignatures.forEach(sigsObj::addProperty);
        object.add("properties", propertiesObj);
        object.add("signatures", sigsObj);

        // Position stuff
        posObj.addProperty("x", this.x);
        posObj.addProperty("y", this.y);
        posObj.addProperty("z", this.z);
        posObj.addProperty("pitch", this.pitch);
        posObj.addProperty("yaw", this.yaw);
        object.add("pos", posObj);

        object.addProperty("gamemode", this.gamemode);
        object.addProperty("ping", this.ping);
        if (this.displayName != null) {
            object.addProperty("display_name", this.displayName);
        }

        // Metadata stuff
        final ByteBuf buffer = Unpooled.buffer();
        this.metadata.write(buffer);
        final byte[] metaBytes = Arrays.copyOf(buffer.array(), buffer.writerIndex());
        buffer.release();
        final String metaStr = Base64.getEncoder().encodeToString(metaBytes);
        object.addProperty("meta", metaStr);

        return GSON.toJson(object);
    }

    /**
     * Overwrite the data of this player wrapper with the data of the provided player wrapper
     *
     * @param other The other player wrapper
     */
    public void overwrite(final PlayerWrapper other) {
        this.properties.clear();
        this.properties.putAll(other.properties);

        this.propertySignatures.clear();
        this.propertySignatures.putAll(other.propertySignatures);

        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.yaw = other.yaw;
        this.pitch = other.pitch;
        this.ping = other.ping;
        this.gamemode = other.gamemode;
        this.displayName = other.displayName;

        this.metadata.overwrite(other.metadata);
    }

    public byte getSkinMask() {
        return (byte) this.metadata.getSafe(17, () ->
                new Metadata.Entry(Metadata.EntryType.BYTE, (byte) 0)).getValue();
    }

    public void setSkinMask(final byte val) {
        this.metadata.set(17, new Metadata.Entry(Metadata.EntryType.BYTE, val));
    }

    public boolean isCapeEnabled() {
        return (this.getSkinMask() & 0x01) == 0x01;
    }

    public void setCapeEnabled(final boolean val) {
        this.setSkinMask(val ? (byte) (this.getSkinMask() | (byte) 0x01) : (byte) (this.getSkinMask() & (byte) ~0x01));
    }

    public boolean isJacketEnabled() {
        return (this.getSkinMask() & 0x02) == 0x02;
    }

    public void setJacketEnabled(final boolean val) {
        this.setSkinMask(val ? (byte) (this.getSkinMask() | (byte) 0x02) : (byte) (this.getSkinMask() & (byte) ~0x02));
    }

    public boolean isLeftSleeveEnabled() {
        return (this.getSkinMask() & 0x04) == 0x04;
    }

    public void setLeftSleeveEnabled(final boolean val) {
        this.setSkinMask(val ? (byte) (this.getSkinMask() | (byte) 0x04) : (byte) (this.getSkinMask() & (byte) ~0x04));
    }

    public boolean isRightSleeveEnabled() {
        return (this.getSkinMask() & 0x08) == 0x08;
    }

    public void setRightSleeveEnabled(final boolean val) {
        this.setSkinMask(val ? (byte) (this.getSkinMask() | (byte) 0x08) : (byte) (this.getSkinMask() & (byte) ~0x08));
    }

    public boolean isLeftLegEnabled() {
        return (this.getSkinMask() & 0x10) == 0x10;
    }

    public void setLeftLegEnabled(final boolean val) {
        this.setSkinMask(val ? (byte) (this.getSkinMask() | (byte) 0x10) : (byte) (this.getSkinMask() & (byte) ~0x10));
    }

    public boolean isRightLegEnabled() {
        return (this.getSkinMask() & 0x20) == 0x20;
    }

    public void setRightLegEnabled(final boolean val) {
        this.setSkinMask(val ? (byte) (this.getSkinMask() | (byte) 0x20) : (byte) (this.getSkinMask() & (byte) ~0x20));
    }

    public boolean isHatEnabled() {
        return (this.getSkinMask() & 0x40) == 0x40;
    }

    public void setHatEnabled(final boolean val) {
        this.setSkinMask(val ? (byte) (this.getSkinMask() | (byte) 0x40) : (byte) (this.getSkinMask() & (byte) ~0x40));
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public Map<String, String> getPropertySignatures() {
        return this.propertySignatures;
    }

    public int getGamemode() {
        return this.gamemode;
    }

    public void setGamemode(final int gamemode) {
        this.gamemode = gamemode;
    }

    public int getPing() {
        return this.ping;
    }

    public void setPing(final int ping) {
        this.ping = ping;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

}
