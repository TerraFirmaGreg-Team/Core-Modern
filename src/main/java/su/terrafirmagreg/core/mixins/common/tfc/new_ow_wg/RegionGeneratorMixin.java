package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.util.RandomSource;

import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGCellular2D;
import su.terrafirmagreg.core.world.new_ow_wg.region.IRegionGenerator;

@Mixin(value = RegionGenerator.class, remap = false)
public abstract class RegionGeneratorMixin implements IRegionGenerator {
    @Unique
    private Settings tfg$settings;
    @Unique
    private Noise2D tfg$oceanicInfluenceNoise;
    @Unique
    private Noise2D tfg$hotSpotAgeNoise;
    @Unique
    private Noise2D tfg$hotSpotIntensityNoise;
    @Unique
    private TFGCellular2D tfg$plateRegionNoise;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void tfg$init(Settings settings, RandomSource random, CallbackInfo ci) {
        tfg$settings = settings;
        tfg$oceanicInfluenceNoise = new OpenSimplex2D(random.nextInt()).spread(0.02f);
        tfg$hotSpotAgeNoise = TFGBiomeNoise.hotSpotAge(random.nextInt()).spread(128);
        tfg$hotSpotIntensityNoise = TFGBiomeNoise.hotSpotIntensity(random.nextInt()).spread(128);
        tfg$plateRegionNoise = TFGBiomeNoise.plateRegions(random.nextInt()).spread(128);
    }

    @Unique
    public Settings tfg$getSettings() {
        return tfg$settings;
    }

    @Unique
    public Noise2D tfg$getOceanicInfluenceNoise() {
        return tfg$oceanicInfluenceNoise;
    }

    @Unique
    public Noise2D tfg$getHotSpotAgeNoise() {
        return tfg$hotSpotAgeNoise;
    }

    @Unique
    public Noise2D tfg$getHotSpotIntensityNoise() {
        return tfg$hotSpotIntensityNoise;
    }

    @Unique
    public TFGCellular2D tfg$getPlateRegionNoise() {
        return tfg$plateRegionNoise;
    }
}
