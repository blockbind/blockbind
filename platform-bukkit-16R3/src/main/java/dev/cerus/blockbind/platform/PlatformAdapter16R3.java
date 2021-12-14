package dev.cerus.blockbind.platform;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.cerus.blockbind.api.entity.Entity;
import dev.cerus.blockbind.api.entity.LivingEntity;
import dev.cerus.blockbind.api.entity.Metadata;
import dev.cerus.blockbind.api.packet.entity.EntityMovePacket;
import dev.cerus.blockbind.api.packet.entity.EntityMoveRotPacket;
import dev.cerus.blockbind.api.packet.entity.EntityRotPacket;
import dev.cerus.blockbind.api.packet.player.PlayerInfoPacket;
import dev.cerus.blockbind.api.packet.player.SpawnPlayerPacket;
import dev.cerus.blockbind.api.platform.PlatformAdapter;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.unref.ObjectBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherObject;
import net.minecraft.server.v1_16_R3.DataWatcherRegistry;
import net.minecraft.server.v1_16_R3.DataWatcherSerializer;
import net.minecraft.server.v1_16_R3.EntityPose;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlatformAdapter16R3 implements PlatformAdapter {

    private final Map<Metadata.EntryType, DataWatcherSerializer<?>> serializerMap = Map.of(
            Metadata.EntryType.BYTE, DataWatcherRegistry.a,
            Metadata.EntryType.INT, DataWatcherRegistry.b,
            Metadata.EntryType.LONG, DataWatcherRegistry.b, // Will probably cause problems
            Metadata.EntryType.FLOAT, DataWatcherRegistry.c,
            Metadata.EntryType.DOUBLE, DataWatcherRegistry.c, // Will probably cause problems
            Metadata.EntryType.STRING, DataWatcherRegistry.d,
            Metadata.EntryType.CHAT, DataWatcherRegistry.e,
            Metadata.EntryType.OPTIONAL_CHAT, DataWatcherRegistry.f,
            Metadata.EntryType.BOOLEAN, DataWatcherRegistry.i,
            Metadata.EntryType.POSE, DataWatcherRegistry.s
    );
    private final Map<Integer, Integer> metaKeyIndexMap = Map.of(
            Entity.META_KEY_MASK, 0,
            Entity.META_KEY_AIR, 1,
            Entity.META_KEY_CUSTOM_NAME, 2,
            Entity.META_KEY_CUSTOM_NAME_VISIBLE, 3,
            Entity.META_KEY_SILENT, 4,
            Entity.META_KEY_NO_GRAVITY, 5,
            Entity.META_KEY_POSE, 6,

            LivingEntity.META_KEY_HAND, 7,
            LivingEntity.META_KEY_HEALTH, 8,

            PlayerWrapper.META_KEY_SKIN, 16
    );

    @Override
    public byte getSkinParts(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        final DataWatcher dataWatcher = ((CraftPlayer) player).getHandle().getDataWatcher();
        return dataWatcher.get(DataWatcherRegistry.a.a(this.metaKeyIndexMap.get(PlayerWrapper.META_KEY_SKIN)));
    }

    @Override
    public Map<String, String> getPlayerProperties(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        return ((CraftPlayer) player).getHandle().getProfile().getProperties().entries()
                .stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, String> getPlayerPropertySignatures(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        return ((CraftPlayer) player).getHandle().getProfile().getProperties().entries()
                .stream()
                .filter(e -> e.getValue().hasSignature())
                .map(e -> Map.entry(e.getKey(), e.getValue().getSignature()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void sendPlayerInfo(final PlayerInfoPacket packet, final PlayerWrapper player) {
        final GameProfile profile = new GameProfile(packet.getUuid(), player.getName());
        player.getProperties().forEach((key, val) ->
                profile.getProperties().put(key, new Property(val, key,
                        player.getPropertySignatures().getOrDefault(key, null))));

        // Can't reference the class directly because the compiler will shit itself
        final Class<?> infoDataClass;
        try {
            infoDataClass = Class.forName("net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo$PlayerInfoData");
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        final PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = switch (packet.getType()) {
            case ADD_PLAYER -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER;
            case REMOVE_PLAYER -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER;
            case UPDATE_GAMEMODE -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE;
            case UPDATE_LATENCY -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY;
            case UPDATE_DISPLAYNAME -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME;
        };

        final Object nmsPacket = new ObjectBuilder(PacketPlayOutPlayerInfo.class)
                .unsafeInstantiate()
                .modify()
                .field("a", action)
                .field("b", List.of(
                        new ObjectBuilder(infoDataClass)
                                .unsafeInstantiate()
                                .modify()
                                .field("d", profile)
                                .field("b", player.getPing())
                                .field("c", EnumGamemode.getById(player.getGamemode()))
                                .field("e", IChatBaseComponent.ChatSerializer.a(player.getDisplayName()))
                                .finish()
                ))
                .finish();
        this.broadcastPacket(nmsPacket);
    }

    @Override
    public void spawnPlayer(final SpawnPlayerPacket packet, final Collection<UUID> receiver) {
        final Object nmsPacket = new ObjectBuilder(PacketPlayOutNamedEntitySpawn.class)
                .unsafeInstantiate()
                .modify()
                .field("a", packet.getEntityId())
                .field("b", packet.getUuid())
                .field("c", packet.getX())
                .field("d", packet.getY())
                .field("e", packet.getZ())
                .field("f", (byte) ((int) (packet.getYaw() * 256.0F / 360.0F)))
                .field("g", (byte) ((int) (packet.getPitch() * 256.0F / 360.0F)))
                .finish();
        this.broadcastPacket(nmsPacket, receiver.stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList()));
    }

    @Override
    public void sendRelMove(final EntityMovePacket relMovePacket, final Collection<UUID> receiver) {
        final double oldX = relMovePacket.getOldX();
        final double oldY = relMovePacket.getOldY();
        final double oldZ = relMovePacket.getOldZ();
        final double currX = relMovePacket.getNewX();
        final double currY = relMovePacket.getNewY();
        final double currZ = relMovePacket.getNewZ();

        final Object nmsPacket = new ObjectBuilder(PacketPlayOutEntity.PacketPlayOutRelEntityMove.class)
                .unsafeInstantiate()
                .modify()
                .superField("a", relMovePacket.getEntityId())
                .superField("b", (short) ((currX * 32 - oldX * 32) * 128))
                .superField("c", (short) ((currY * 32 - oldY * 32) * 128))
                .superField("d", (short) ((currZ * 32 - oldZ * 32) * 128))
                .superField("g", true)
                .finish();
        this.broadcastPacket(nmsPacket, receiver.stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList()));
    }

    @Override
    public void sendRelMoveRot(final EntityMoveRotPacket relMoveRotPacket, final Collection<UUID> receiver) {
        final double oldX = relMoveRotPacket.getOldX();
        final double oldY = relMoveRotPacket.getOldY();
        final double oldZ = relMoveRotPacket.getOldZ();
        final double currX = relMoveRotPacket.getNewX();
        final double currY = relMoveRotPacket.getNewY();
        final double currZ = relMoveRotPacket.getNewZ();

        final Object nmsPacket = new ObjectBuilder(PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook.class)
                .unsafeInstantiate()
                .modify()
                .superField("a", relMoveRotPacket.getEntityId())
                .superField("b", (short) ((currX * 32 - oldX * 32) * 128))
                .superField("c", (short) ((currY * 32 - oldY * 32) * 128))
                .superField("d", (short) ((currZ * 32 - oldZ * 32) * 128))
                .superField("e", (byte) ((int) (relMoveRotPacket.getYaw() * 256.0F / 360.0F)))
                .superField("f", (byte) ((int) (relMoveRotPacket.getPitch() * 256.0F / 360.0F)))
                .superField("g", true)
                .finish();
        this.broadcastPacket(nmsPacket, receiver.stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList()));
    }

    @Override
    public void sendRot(final EntityRotPacket rotPacket, final PlayerWrapper player, final Collection<UUID> receiver) {
        final Object nmsPacket = new ObjectBuilder(PacketPlayOutEntity.PacketPlayOutEntityLook.class)
                .unsafeInstantiate()
                .modify()
                .superField("a", player.getEntityId())
                .superField("e", (byte) ((int) (rotPacket.getYaw() * 256.0F / 360.0F)))
                .superField("f", (byte) ((int) (rotPacket.getPitch() * 256.0F / 360.0F)))
                .superField("g", true)
                .finish();
        this.broadcastPacket(nmsPacket, receiver.stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList()));
    }

    @Override
    public void sendHeadRot(final PlayerWrapper player, final Collection<UUID> receiver) {
        final Object nmsPacket = new ObjectBuilder(PacketPlayOutEntityHeadRotation.class)
                .unsafeInstantiate()
                .modify()
                .field("a", player.getEntityId())
                .field("b", (byte) ((int) (player.getYaw() * 256.0F / 360.0F)))
                .finish();
        this.broadcastPacket(nmsPacket, receiver.stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList()));
    }

    @Override
    public void destroyEntity(final int[] ids, final Collection<UUID> receiver) {
        final Object nmsPacket = new PacketPlayOutEntityDestroy(ids);
        this.broadcastPacket(nmsPacket, receiver.stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList()));
    }

    @Override
    public void sendMetadata(final int eid, final Metadata metadata, final Collection<UUID> receiver) {
        final List<DataWatcher.Item<?>> items = new ArrayList<>();
        metadata.forEach((idx, entry) -> {
            final DataWatcherSerializer<Object> serializer = (DataWatcherSerializer<Object>) this.serializerMap.get(entry.getType());
            final DataWatcherObject<Object> obj = serializer.a((int) this.metaKeyIndexMap.get(idx));
            Object val = entry.getValue();
            if (entry.getType() == Metadata.EntryType.CHAT) {
                val = IChatBaseComponent.ChatSerializer.a((String) val);
            } else if (entry.getType() == Metadata.EntryType.OPTIONAL_CHAT) {
                val = entry.getValue() == null ? Optional.empty() : Optional.ofNullable(IChatBaseComponent.ChatSerializer.a((String) val));
            } else if (entry.getType() == Metadata.EntryType.POSE) {
                val = switch ((byte) entry.getValue()) {
                    case 1 -> EntityPose.FALL_FLYING;
                    case 2 -> EntityPose.SLEEPING;
                    case 3 -> EntityPose.SWIMMING;
                    case 4 -> EntityPose.SPIN_ATTACK;
                    case 5 -> EntityPose.CROUCHING;
                    case 7 -> EntityPose.DYING;
                    default -> EntityPose.STANDING;
                };
            }
            items.add(new DataWatcher.Item<>(obj, val));
        });
        final Object nmsPacket = new ObjectBuilder(PacketPlayOutEntityMetadata.class)
                .unsafeInstantiate()
                .modify()
                .field("a", eid)
                .field("b", items)
                .finish();
        this.broadcastPacket(nmsPacket, receiver.stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList()));
    }

    private void broadcastPacket(final Object o) {
        this.broadcastPacket(o, Bukkit.getOnlinePlayers());
    }

    private void broadcastPacket(final Object o, final Collection<? extends Player> players) {
        for (final Player player : players) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) o);
        }
    }

}
