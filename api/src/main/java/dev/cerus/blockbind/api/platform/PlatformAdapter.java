package dev.cerus.blockbind.api.platform;

import dev.cerus.blockbind.api.packet.entity.EntityMovePacket;
import dev.cerus.blockbind.api.packet.entity.EntityMoveRotPacket;
import dev.cerus.blockbind.api.packet.entity.EntityRotPacket;
import dev.cerus.blockbind.api.packet.player.PlayerInfoPacket;
import dev.cerus.blockbind.api.packet.player.SpawnPlayerPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PlatformAdapter {

    Map<String, String> getPlayerProperties(UUID uuid);

    Map<String, String> getPlayerPropertySignatures(UUID uuid);

    void sendPlayerInfo(PlayerInfoPacket packet, PlayerWrapper player);

    void spawnPlayer(SpawnPlayerPacket packet, Collection<UUID> receiver);

    void sendRelMove(EntityMovePacket relMovePacket, Collection<UUID> receiver);

    void sendRelMoveRot(EntityMoveRotPacket relMoveRotPacket, Collection<UUID> receiver);

    void sendRot(EntityRotPacket rotPacket, PlayerWrapper player, Collection<UUID> receiver);

    void sendHeadRot(PlayerWrapper player, Collection<UUID> receiver);

    void destroyEntity(int[] ids, Collection<UUID> receiver);

}
