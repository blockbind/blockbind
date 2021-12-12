package dev.cerus.blockbind.api.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerWrapper {

    private static final Gson GSON = new GsonBuilder()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create();

    private final UUID uuid;
    private final String name;
    private final int entityId;
    private final Map<String, String> properties;
    private final Map<String, String> propertySignatures;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private int gamemode;
    private int ping;
    private String displayName;

    public PlayerWrapper(final UUID uuid, final String name, final int entityId, final Map<String, String> properties, final Map<String, String> propertySignatures) {
        this.uuid = uuid;
        this.name = name;
        this.entityId = entityId;
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
        this.uuid = uuid;
        this.name = name;
        this.entityId = entityId;
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

    public static PlayerWrapper parse(final JsonObject playerObj) {
        final JsonObject uuidObj = playerObj.get("uuid").getAsJsonObject();
        final JsonObject propertiesObj = playerObj.get("properties").getAsJsonObject();
        final JsonObject sigsObj = playerObj.get("signatures").getAsJsonObject();
        final JsonObject posObj = playerObj.get("pos").getAsJsonObject();

        final Map<String, String> properties = new HashMap<>();
        final Map<String, String> signatures = new HashMap<>();
        for (final String key : propertiesObj.keySet()) {
            properties.put(key, propertiesObj.get(key).getAsString());
        }
        for (final String key : sigsObj.keySet()) {
            signatures.put(key, sigsObj.get(key).getAsString());
        }

        return new PlayerWrapper(
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
    }

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

        this.properties.forEach(propertiesObj::addProperty);
        this.propertySignatures.forEach(sigsObj::addProperty);
        object.add("properties", propertiesObj);
        object.add("signatures", sigsObj);

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

        return GSON.toJson(object);
    }

    public void overwrite(final PlayerWrapper player) {
        this.properties.clear();
        this.properties.putAll(player.properties);

        this.propertySignatures.clear();
        this.propertySignatures.putAll(player.propertySignatures);

        this.x = player.x;
        this.y = player.y;
        this.z = player.z;
        this.yaw = player.yaw;
        this.pitch = player.pitch;
        this.ping = player.ping;
        this.gamemode = player.gamemode;
        this.displayName = player.displayName;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public Map<String, String> getPropertySignatures() {
        return this.propertySignatures;
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
