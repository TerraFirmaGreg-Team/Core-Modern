package su.terrafirmagreg.core.common.data.screens;

import java.util.*;

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
import lombok.Getter;

import su.terrafirmagreg.core.common.data.utils.LaunchPositionHandler;

public class TFGPlanetsMenu extends PlanetsMenu {
    protected final Set<CompoundTag> allPlanetsPosData;
    @Getter
    protected Map<ResourceKey<Level>, List<List<Object>>> planetLandingPos;

    public TFGPlanetsMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, PlanetsMenuProvider.createDisabledPlanetsFromBuf(buf), PlanetsMenuProvider.createSpaceStationsFromBuf(buf), PlanetsMenuProvider.createClaimedChunksFromBuf(buf),
                PlanetsMenuProvider.createSpawnLocationsFromBuf(buf), LaunchPositionHandler.getPlanetPosDataFromBuffer(buf));
    }

    public TFGPlanetsMenu(int containerId, Inventory inventory, Set<ResourceLocation> disabledPlanets, Map<ResourceKey<Level>, Map<UUID, Set<SpaceStation>>> spaceStations,
            Object2BooleanMap<ResourceKey<Level>> claimedChunks, Set<GlobalPos> spawnLocations, Set<CompoundTag> planetPosData) {
        super(containerId, inventory, disabledPlanets, spaceStations, claimedChunks, spawnLocations);
        this.allPlanetsPosData = planetPosData;
        this.planetLandingPos = mapPlanetPosData();
    }

    public Map<ResourceKey<Level>, List<List<Object>>> mapPlanetPosData() {
        Map<ResourceKey<Level>, List<List<Object>>> planetLandingPos = new HashMap<>();

        allPlanetsPosData.forEach((singlePlanetTag) -> {
            List<List<Object>> singlePlanetData = LaunchPositionHandler.unpackagePlanetPosData(singlePlanetTag);

            System.out.println(singlePlanetData);
            if (singlePlanetData.get(0).get(0) instanceof GlobalPos pos) {
                ResourceKey<Level> planetKey = pos.dimension();
                planetLandingPos.put(planetKey, singlePlanetData);
            }
        });

        System.out.println(planetLandingPos);
        return planetLandingPos;
    }
}
