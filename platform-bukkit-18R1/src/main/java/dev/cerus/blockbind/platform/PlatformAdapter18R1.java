package dev.cerus.blockbind.platform;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.cerus.blockbind.api.packet.entity.EntityMovePacket;
import dev.cerus.blockbind.api.packet.entity.EntityMoveRotPacket;
import dev.cerus.blockbind.api.packet.entity.EntityRotPacket;
import dev.cerus.blockbind.api.packet.player.PlayerInfoPacket;
import dev.cerus.blockbind.api.packet.player.SpawnPlayerPacket;
import dev.cerus.blockbind.api.platform.PlatformAdapter;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import dev.cerus.unref.ObjectBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlatformAdapter18R1 implements PlatformAdapter {

    @Override
    public Map<String, String> getPlayerProperties(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        return ((CraftPlayer) player).getHandle().fp().getProperties().entries()
                .stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, String> getPlayerPropertySignatures(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        return ((CraftPlayer) player).getHandle().fp().getProperties().entries()
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

        final PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = switch (packet.getType()) {
            case ADD_PLAYER -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a;
            case REMOVE_PLAYER -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e;
            case UPDATE_GAMEMODE -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.b;
            case UPDATE_LATENCY -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.c;
            case UPDATE_DISPLAYNAME -> PacketPlayOutPlayerInfo.EnumPlayerInfoAction.d;
        };

        final Object nmsPacket = new ObjectBuilder(PacketPlayOutPlayerInfo.class)
                .unsafeInstantiate()
                .modify()
                .field("a", action)
                .field("b", List.of(
                        new PacketPlayOutPlayerInfo.PlayerInfoData(
                                profile,
                                player.getPing(),
                                EnumGamemode.a(player.getGamemode()),
                                IChatBaseComponent.ChatSerializer.a(player.getDisplayName())
                        )
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

        final Object nmsPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                relMovePacket.getEntityId(),
                (short) ((currX * 32 - oldX * 32) * 128),
                (short) ((currY * 32 - oldY * 32) * 128),
                (short) ((currZ * 32 - oldZ * 32) * 128),
                true
        );
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

        final Object nmsPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                relMoveRotPacket.getEntityId(),
                (short) ((currX * 32 - oldX * 32) * 128),
                (short) ((currY * 32 - oldY * 32) * 128),
                (short) ((currZ * 32 - oldZ * 32) * 128),
                (byte) ((int) (relMoveRotPacket.getYaw() * 256.0F / 360.0F)),
                (byte) ((int) (relMoveRotPacket.getPitch() * 256.0F / 360.0F)),
                true
        );
        this.broadcastPacket(nmsPacket, receiver.stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList()));
    }

    @Override
    public void sendRot(final EntityRotPacket rotPacket, final PlayerWrapper player, final Collection<UUID> receiver) {
        final Object nmsPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                player.getEntityId(),
                (byte) ((int) (player.getYaw() * 256.0F / 360.0F)),
                (byte) ((int) (player.getPitch() * 256.0F / 360.0F)),
                true
        );
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

    private void broadcastPacket(final Object o) {
        this.broadcastPacket(o, Bukkit.getOnlinePlayers());
    }

    private void broadcastPacket(final Object o, final Collection<? extends Player> players) {
        for (final Player player : players) {
            ((CraftPlayer) player).getHandle().b.a((Packet<?>) o);
        }
    }

}
