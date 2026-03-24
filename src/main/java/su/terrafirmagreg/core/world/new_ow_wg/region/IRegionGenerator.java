package su.terrafirmagreg.core.world.new_ow_wg.region;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.settings.Settings;

import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGCellular2D;

public interface IRegionGenerator {
    Settings tfg$getSettings();

    Noise2D tfg$getOceanicInfluenceNoise();

    Noise2D tfg$getHotSpotAgeNoise();

    Noise2D tfg$getHotSpotIntensityNoise();

    TFGCellular2D tfg$getPlateRegionNoise();
}
