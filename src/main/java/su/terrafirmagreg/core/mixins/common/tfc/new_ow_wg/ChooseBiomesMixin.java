package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import static net.dries007.tfc.world.layer.TFCLayers.*;
import static su.terrafirmagreg.core.world.new_ow_wg.TFGLayers.*;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.world.layer.framework.Area;
import net.dries007.tfc.world.region.ChooseBiomes;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.world.new_ow_wg.TFGLayers;

@Mixin(value = ChooseBiomes.class, remap = false)
public abstract class ChooseBiomesMixin {

    // These are identical between 1.20 and 1.21
    @Shadow
    @Final
    private static int[] MOUNTAIN_ALTITUDE_BIOMES;
    @Shadow
    @Final
    private static int[] OCEANIC_MOUNTAIN_ALTITUDE_BIOMES;

    @Shadow
    protected abstract int randomSeededFrom(long rngSeed, int areaSeed, int[] choices);

    @Unique
    private static final int[][] TFG_ALTITUDE_BIOMES = {
            { PLAINS, PLAINS, HILLS, ROLLING_HILLS, LOW_CANYONS, LOWLANDS, MUD_FLATS, SALT_FLATS }, // Low
            { PLAINS, HILLS, TFGLayers.DEEP_OCEAN, TFGLayers.DEEP_OCEAN, TFGLayers.OCEAN, TFGLayers.OCEAN, PLATEAU, CANYONS, LOW_CANYONS }, // Mid
            { HIGHLANDS, ROLLING_HILLS, BADLANDS, PLATEAU, PLATEAU, OLD_MOUNTAINS, OLD_MOUNTAINS, DUNE_SEA, GRASSY_DUNES }, // High
    };

    @Unique
    private static final int[] TFG_MID_DEPTH_OCEAN_BIOMES = { TFGLayers.DEEP_OCEAN, TFGLayers.OCEAN, TFGLayers.OCEAN, TFGLayers.OCEAN_REEF, TFGLayers.OCEAN_REEF, TFGLayers.OCEAN_REEF };

    // TODO: add guano
    @Unique
    private static final int[] TFG_ISLAND_BIOMES = { PLAINS, HILLS, ROLLING_HILLS, VOLCANIC_OCEANIC_MOUNTAINS, VOLCANIC_OCEANIC_MOUNTAINS };

    @Inject(method = "apply", at = @At("HEAD"), remap = false)
    public void tfg$apply(RegionGenerator.Context context, CallbackInfo ci) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {

            final Region region = context.region;
            final Area blobArea = context.generator().biomeArea.get();
            final long rngSeed = context.random.nextLong();
            final long climateSeed = context.random.nextLong();

            for (int x = region.minX(); x <= region.maxX(); x++) {
                for (int z = region.minZ(); z <= region.maxZ(); z++) {
                    final Region.Point point = region.maybeAt(x, z);
                    if (point != null) {
                        final int areaSeed = blobArea.get(x, z);
                        if (point.island()) {
                            point.biome = randomSeededFrom(rngSeed, areaSeed, TFG_ISLAND_BIOMES);
                        } else if (point.mountain()) {
                            point.biome = randomSeededFrom(rngSeed, areaSeed, point.coastalMountain()
                                    ? OCEANIC_MOUNTAIN_ALTITUDE_BIOMES
                                    : MOUNTAIN_ALTITUDE_BIOMES);
                        } else if (point.land()) {
                            point.biome = randomSeededFrom(rngSeed, areaSeed, TFG_ALTITUDE_BIOMES[point.discreteBiomeAltitude()]);
                        } else if (point.baseOceanDepth < 3) {
                            point.biome = TFGLayers.OCEAN;
                        } else if (point.baseOceanDepth > 9) {
                            point.biome = TFGLayers.DEEP_OCEAN_TRENCH;
                        } else if (point.baseOceanDepth >= 5 || point.distanceToEdge < 2) {
                            point.biome = TFGLayers.DEEP_OCEAN;
                        } else {
                            point.biome = randomSeededFrom(rngSeed, areaSeed, TFG_MID_DEPTH_OCEAN_BIOMES);
                        }

                        // Adjust certain biome placements by climate. Low, freshwater biomes don't make much sense appearing in
                        // very low rainfall areas, so replace them with slightly higher biomes
                        final float minRainForLowFreshWaterBiomes = 90f + Math.floorMod(areaSeed ^ climateSeed, 40);
                        if (point.rainfall < minRainForLowFreshWaterBiomes) {
                            if (point.biome == LOWLANDS)
                                point.biome = PLAINS;
                            else if (point.biome == LOW_CANYONS)
                                point.biome = CANYONS;
                        }

                        // Prevent badlands from appearing in very high rainfall environments
                        final float maxRainfallForBadlands = 420f + Math.floorMod(areaSeed ^ climateSeed, 40);
                        if (point.rainfall > maxRainfallForBadlands) {
                            if (point.biome == BADLANDS)
                                point.biome = HIGHLANDS;
                            else if (point.biome == INVERTED_BADLANDS)
                                point.biome = ROLLING_HILLS;
                        }
                    }
                }
            }
        }
    }
}
