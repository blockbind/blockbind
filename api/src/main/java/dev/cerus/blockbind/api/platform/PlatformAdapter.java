package dev.cerus.blockbind.api.platform;

import dev.cerus.blockbind.api.entity.Metadata;
import dev.cerus.blockbind.api.packet.entity.EntityMovePacket;
import dev.cerus.blockbind.api.packet.entity.EntityMoveRotPacket;
import dev.cerus.blockbind.api.packet.entity.EntityRotPacket;
import dev.cerus.blockbind.api.packet.player.PlayerInfoPacket;
import dev.cerus.blockbind.api.packet.player.SpawnPlayerPacket;
import dev.cerus.blockbind.api.player.PlayerWrapper;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for platform specific implementations
 */
public interface PlatformAdapter {

    /**
     * Returns the properties of a player
     *
     * @param uuid The player uuid
     *
     * @return A property map
     */
    Map<String, String> getPlayerProperties(UUID uuid);

    /**
     * Returns the property signatures of a player
     *
     * @param uuid The player uuid
     *
     * @return A signature map
     */
    Map<String, String> getPlayerPropertySignatures(UUID uuid);

    /**
     * Gets the skin part mask of a player
     *
     * @param uuid The player uuid
     *
     * @return A bit set
     */
    byte getSkinParts(UUID uuid);

    /**
     * Gets the protocol id of a block
     *
     * @param worldId World ID
     * @param x       X pos
     * @param y       Y pos
     * @param z       Z pos
     *
     * @return Block id
     */
    int getProtocolId(UUID worldId, int x, int y, int z);

    /**
     * Sets a block at specified coords
     *
     * @param worldId World ID
     * @param x       X pos
     * @param y       Y pos
     * @param z       Z pos
     * @param blockId ID of block
     */
    void setBlockAt(UUID worldId, int x, int y, int z, int blockId);

    /**
     * Translates a Block Bind player info packet into the platform equivalent and sends it to chosen receivers
     *
     * @param packet The packet to translate
     * @param player The player in question
     */
    void sendPlayerInfo(PlayerInfoPacket packet, PlayerWrapper player);

    /**
     * Translates a Block Bind player spawn packet into the platform equivalent and sends it to chosen receivers
     *
     * @param packet   The packet to translate
     * @param receiver The list of receivers
     */
    void spawnPlayer(SpawnPlayerPacket packet, Collection<UUID> receiver);

    /**
     * Translates a Block Bind entity move packet into the platform equivalent and sends it to chosen receivers
     *
     * @param movePacket The packet to translate
     * @param receiver   The list of receivers
     */
    void sendMove(EntityMovePacket movePacket, Collection<UUID> receiver);

    /**
     * Translates a Block Bind entity move rotation packet into the platform equivalent and sends it to chosen receivers
     *
     * @param moveRotPacket The packet to translate
     * @param receiver      The list of receivers
     */
    void sendMoveRot(EntityMoveRotPacket moveRotPacket, Collection<UUID> receiver);

    /**
     * Translates a Block Bind entity rotation packet into the platform equivalent and sends it to chosen receivers
     *
     * @param rotPacket The packet to translate
     * @param receiver  The list of receivers
     */
    void sendRot(EntityRotPacket rotPacket, PlayerWrapper player, Collection<UUID> receiver);

    /**
     * Creates a platform specific head rotation packet and sends it to chosen receivers
     *
     * @param player   The player in question
     * @param receiver The list of receivers
     */
    void sendHeadRot(PlayerWrapper player, Collection<UUID> receiver);

    /**
     * Creates a platform specific entity destroy packet and sends it to chosen receivers
     *
     * @param ids      The entity ids to destroy
     * @param receiver The list of receivers
     */
    void destroyEntity(int[] ids, Collection<UUID> receiver);

    /**
     * Creates a platform specific entity metadata packet and sends it to chosen receivers
     *
     * @param eid      The id of the entity id in question
     * @param metadata The metadata of the entity
     * @param receiver The list of receivers
     */
    void sendMetadata(int eid, Metadata metadata, Collection<UUID> receiver);

}
