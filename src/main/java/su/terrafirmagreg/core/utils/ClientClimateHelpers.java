package su.terrafirmagreg.core.utils;

import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;
import earth.terrarium.adastra.api.systems.PlanetData;
import earth.terrarium.adastra.client.utils.ClientData;

public class ClientClimateHelpers {
    /**
     * Gets temperature for client-side tooltips accounting for Ad Astra rooms.
     * Server-side can and should use Climate.getTemperature directly.
     * This uses the local copy of the player's own oxygen status. The assumption is that if the player is currently
     *      oxygenated, then the block they're looking at is as well. This should be mostly true in sealed rooms
     *      without freecam/spectator mode.
     */
    public static float getTemperatureForTooltip(Level level, BlockPos pos) {
        if (level instanceof ServerLevel) {
            return Climate.getTemperature(level, pos);
        }

        Planet planet = PlanetApi.API.getPlanet(level);
        if (planet != null && !planet.oxygen()) {
            // Airless planet, use player's oxygen status instead of block's because block's is unknown clientside.
            PlanetData localData = ClientData.getLocalData();
            if (localData != null && localData.oxygen()) {
                return localData.temperature();
            }
        }

        return Climate.getTemperature(level, pos);
    }
}
