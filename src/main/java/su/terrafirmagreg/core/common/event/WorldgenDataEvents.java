package su.terrafirmagreg.core.common.event;

import java.nio.file.Files;
import java.util.*;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.minecraft.AccessorMinecraftServer;
import su.terrafirmagreg.core.world.WorldgenData;

public class WorldgenDataEvents {

    // Warnings about changed worldgen overrides, shown to ops on login.
    // Cleared at server start so stale warnings from previous sessions don't persist.
    private static final List<String> pendingOpWarnings = new ArrayList<>();

    /** Ops who already received {@link #pendingOpWarnings} this server session — avoid repeating on reconnect. */
    private static final Set<UUID> opWarningsSentThisSession = new HashSet<>();

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        final MinecraftServer server = event.getServer();
        final int configOverride = TFGConfig.SERVER.mountainScalingOverride.get();

        // server.overworld() doesn't exist yet, but we need the data before initRandomState fires
        //  during ServerLevel construction.
        final var storageAccess = ((AccessorMinecraftServer) server).tfg$getStorageSource();
        final var storage = new DimensionDataStorage(
                storageAccess.getDimensionPath(Level.OVERWORLD).resolve("data").toFile(),
                server.getFixerUpper());
        final WorldgenData data = storage.computeIfAbsent(
                WorldgenData::new, WorldgenData::new, WorldgenData.MOUNTAIN_SCALING_ID);

        if (configOverride != WorldgenData.MOUNTAIN_SCALING_UNSET) {
            WorldgenData.MOUNTAIN_SCALING = configOverride;
        } else {
            // Empty generatedVersion means either a new world or a pre-worldgenversion world.
            // isNewWorld distinguishes the two: new worlds default to the latest version, old worlds to 0.
            final boolean isNewWorld = !Files.exists(storageAccess.getDimensionPath(Level.OVERWORLD).resolve("region"));
            final int defaultScaling = isNewWorld ? WorldgenData.MOUNTAIN_SCALING_NEW_WORLD_DEFAULT : WorldgenData.MOUNTAIN_SCALING_TFC_DEFAULTS;

            var generated = data.getGeneratedMountainScaling();
            WorldgenData.MOUNTAIN_SCALING = generated == WorldgenData.MOUNTAIN_SCALING_UNSET ? defaultScaling : generated;
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        final MinecraftServer server = event.getServer();
        final WorldgenData data = WorldgenData.get(server);
        final int configOverride = TFGConfig.SERVER.mountainScalingOverride.get();

        // Handle first boot
        if (data.getGeneratedMountainScaling() == WorldgenData.MOUNTAIN_SCALING_UNSET) {
            data.setGeneratedMountainScaling(WorldgenData.MOUNTAIN_SCALING);
        }

        // Collect warnings for ops about changed worldgen overrides
        final int knownOverride = data.getKnownConfigOverrideMountainScaling();

        final boolean changed = configOverride != WorldgenData.MOUNTAIN_SCALING_UNSET && configOverride != knownOverride
                || configOverride == WorldgenData.MOUNTAIN_SCALING_UNSET && knownOverride != WorldgenData.MOUNTAIN_SCALING_UNSET;

        if (changed) {
            final String from = knownOverride != WorldgenData.MOUNTAIN_SCALING_UNSET ? String.valueOf(knownOverride) : "<none>";
            final String to = configOverride != WorldgenData.MOUNTAIN_SCALING_UNSET ? String.valueOf(configOverride) : "<none>";
            final String msg = "[TFG] Mountain scaling override changed from " + from + " to " + to
                    + ". If unintentional, restore your config before generating new chunks.";

            TFGCore.LOGGER.warn(msg);
            pendingOpWarnings.add(msg);
            data.setKnowConfigMountainScaling(configOverride);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (pendingOpWarnings.isEmpty())
            return;
        final MinecraftServer server = player.getServer();
        if (server == null || !server.getPlayerList().isOp(player.getGameProfile()))
            return;
        if (!opWarningsSentThisSession.add(player.getUUID())) {
            return;
        }

        for (String msg : pendingOpWarnings) {
            player.sendSystemMessage(Component.literal(msg));
        }
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        WorldgenData.MOUNTAIN_SCALING = WorldgenData.MOUNTAIN_SCALING_UNSET;
        pendingOpWarnings.clear();
        opWarningsSentThisSession.clear();
    }
}
