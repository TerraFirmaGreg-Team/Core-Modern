package su.terrafirmagreg.core.common.data.events;

import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.minecraft.AccessorMinecraftServer;
import su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData;

public class WorldgenVersionEvents {

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        final MinecraftServer server = event.getServer();
        final Map<ResourceLocation, Integer> configOverrides = TFGConfig.SERVER.parsedWorldgenOverrides();
        final ResourceLocation overworld = Level.OVERWORLD.location();

        // server.overworld() doesn't exist yet, but we need the data before initRandomState fires
        //  during ServerLevel construction.
        final var storageAccess = ((AccessorMinecraftServer) server).tfg$getStorageSource();
        final var storage = new DimensionDataStorage(
                storageAccess.getDimensionPath(Level.OVERWORLD).resolve("data").toFile(),
                server.getFixerUpper());
        final WorldgenVersionData data = storage.computeIfAbsent(
                WorldgenVersionData::new, WorldgenVersionData::new, WorldgenVersionData.DATA_ID);

        final Integer configOverride = configOverrides.get(overworld);
        if (configOverride != null) {
            WorldgenVersionData.OVERWORLD_VERSION = configOverride;
        } else {
            // Empty generatedVersion means either a new world or a pre-worldgenversion world.
            // isNewWorld distinguishes the two: new worlds default to the latest version, old worlds to 0.
            final boolean isNewWorld = !Files.exists(storageAccess.getDimensionPath(Level.OVERWORLD).resolve("region"));
            final int defaultVersion = isNewWorld ? WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT : 0;
            WorldgenVersionData.OVERWORLD_VERSION = data.getGeneratedVersion(overworld).orElse(defaultVersion);
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        final MinecraftServer server = event.getServer();
        final WorldgenVersionData data = WorldgenVersionData.get(server);
        final Map<ResourceLocation, Integer> configOverrides = TFGConfig.SERVER.parsedWorldgenOverrides();

        // First boot under this system: generatedVersions is entirely absent.
        // Record all currently loaded dimensions at once. The overworld gets the resolved session version;
        // all others get 0. This also handles pre-system worlds (old worlds never written to before).
        if (data.getGeneratedVersion(Level.OVERWORLD.location()).isEmpty()) {
            for (ServerLevel level : server.getAllLevels()) {
                final int version = level.dimension() == Level.OVERWORLD
                        ? WorldgenVersionData.OVERWORLD_VERSION
                        : 0;
                data.setGeneratedVersion(level.dimension().location(), version);
            }
        }

        // Warn ops if config overrides changed since last session.
        final Set<ResourceLocation> dimensions = new HashSet<>(configOverrides.keySet());
        dimensions.addAll(data.knownOverrideDimensions());

        for (ResourceLocation dim : dimensions) {
            final Integer currentOverride = configOverrides.get(dim);
            final OptionalInt knownOverride = data.getKnownConfigOverride(dim);

            final boolean changed = currentOverride != null && knownOverride.isEmpty()
                    || currentOverride != null && currentOverride != knownOverride.getAsInt()
                    || currentOverride == null && knownOverride.isPresent();

            if (changed) {
                final String from = knownOverride.isPresent() ? String.valueOf(knownOverride.getAsInt()) : "<none>";
                final String to = currentOverride != null ? String.valueOf(currentOverride) : "<none>";
                final String msg = "[TFG] Worldgen override for " + dim + " changed from " + from + " to " + to
                        + ". If unintentional, restore your config before generating new chunks.";

                TFGCore.LOGGER.warn(msg);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (server.getPlayerList().isOp(player.getGameProfile())) {
                        player.sendSystemMessage(Component.literal(msg));
                    }
                }

                data.setKnownConfigOverride(dim, currentOverride);
            }
        }
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        WorldgenVersionData.OVERWORLD_VERSION = 0;
    }
}
