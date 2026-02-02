package su.terrafirmagreg.core.compat.ae2;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.events.GridSpatialEvent;
import appeng.api.networking.spatial.ISpatialService;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ClaimedChunkManager;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamRank;

/**
 * Prevent Spatial IO events from affecting claimed chunks
 * Checks who owns the Spatial IO Port (who placed it),
 *  and checks if that person is allowed to affect the claimed chunks.
 */
public class AE2Compat {

    public static void registerEvents() {
        GridHelper.addEventHandler(GridSpatialEvent.class, AE2Compat::onGridSpatialEvent);
    }

    private static void onGridSpatialEvent(IGrid grid, GridSpatialEvent event) {
        if (!(event.spatialIoLevel instanceof ServerLevel serverLevel))
            return;

        ISpatialService spatial = grid.getSpatialService();
        if (!spatial.isValidRegion())
            return;

        // Get the node for the Spatial IO port that triggered this event
        IInWorldGridNodeHost host = GridHelper.getNodeHost(serverLevel, event.spatialIoPos);
        if (host == null)
            return;

        IGridNode node = host.getGridNode(Direction.UP); // Any direction works for Spatial IO
        if (node == null)
            return;

        UUID spatialPortOwner = node.getOwningPlayerProfileId();

        BlockPos min = spatial.getMin();
        BlockPos max = spatial.getMax();
        Level captureLevel = spatial.getLevel();

        int minChunkX = SectionPos.blockToSectionCoord(min.getX());
        int minChunkZ = SectionPos.blockToSectionCoord(min.getZ());
        int maxChunkX = SectionPos.blockToSectionCoord(max.getX());
        int maxChunkZ = SectionPos.blockToSectionCoord(max.getZ());

        ClaimedChunkManager chunkManager = FTBChunksAPI.api().getManager();
        ResourceKey<Level> dimension = captureLevel.dimension();

        // Iterate over all affected chunks
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                ChunkDimPos dimPos = new ChunkDimPos(dimension, cx, cz);

                ClaimedChunk claimed = chunkManager.getChunk(dimPos);
                if (claimed != null && !canAffectClaim(claimed, spatialPortOwner)) {
                    // Player is not allowed to affect this chunk, prevent transition
                    event.preventTransition();

                    // Notify player if possible
                    if (spatialPortOwner != null) {
                        ServerPlayer serverPlayer = serverLevel.getServer().getPlayerList().getPlayer(spatialPortOwner);

                        if (serverPlayer != null) {
                            serverPlayer.displayClientMessage(Component.translatable("tfg.clientmessage.spatialioblocked.claimedchunks")
                                    .withStyle(ChatFormatting.RED), true);
                        }
                    }

                    return;
                }
            }
        }
    }

    /** Checks whether this player is allowed to affect this claimed chunk */
    private static boolean canAffectClaim(ClaimedChunk claimed, @Nullable UUID playerUUID) {
        if (playerUUID == null) {
            return false;
        }

        Team team = claimed.getTeamData().getTeam();

        if (team.isPlayerTeam() && playerUUID.equals(team.getId())) {
            return true;
        }

        TeamRank rank = team.getRankForPlayer(playerUUID);
        return rank.isMemberOrBetter();
    }
}
