package su.terrafirmagreg.core.common.data.screens;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.handlers.base.SpaceStation;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import earth.terrarium.adastra.common.menus.base.PlanetsMenuProvider;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

import su.terrafirmagreg.core.common.data.utils.LaunchPositionHandler;

public class TFGPlanetsMenu extends PlanetsMenu {
    protected final Set<CompoundTag> planetPosData;

    public TFGPlanetsMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, PlanetsMenuProvider.createDisabledPlanetsFromBuf(buf), PlanetsMenuProvider.createSpaceStationsFromBuf(buf), PlanetsMenuProvider.createClaimedChunksFromBuf(buf),
                PlanetsMenuProvider.createSpawnLocationsFromBuf(buf), LaunchPositionHandler.getPlanetPosDataFromBuffer(buf));
    }

    public TFGPlanetsMenu(int containerId, Inventory inventory, Set<ResourceLocation> disabledPlanets, Map<ResourceKey<Level>, Map<UUID, Set<SpaceStation>>> spaceStations,
            Object2BooleanMap<ResourceKey<Level>> claimedChunks, Set<GlobalPos> spawnLocations, Set<CompoundTag> planetPosData) {
        super(containerId, inventory, disabledPlanets, spaceStations, claimedChunks, spawnLocations);
        this.planetPosData = planetPosData;
    }
}
