package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

import java.util.function.LongFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.BiomeNoiseSampler;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeBuilder;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.dries007.tfc.world.surface.builder.VolcanoesSurfaceBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.world.new_ow_wg.*;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.TuffRingsSurfaceBuilder;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.TuyasSurfaceBuilder;

/**
 * Adds additional data to biome builders that are new in 1.21
 */

@Mixin(value = BiomeBuilder.class, remap = false)
public class BiomeBuilderMixin implements IBiomeBuilder {
    @Shadow
    private LongFunction<Noise2D> heightNoiseFactory;
    @Shadow
    private LongFunction<BiomeNoiseSampler> noiseFactory;
    @Shadow
    private SurfaceBuilderFactory surfaceBuilderFactory;
    @Shadow
    private int volcanoBasaltHeight;
    @Shadow
    private boolean volcanic;
    @Shadow
    private int volcanoFrequency;

    @Unique
    private ShoreBlendType tfg$shoreBlendType;
    @Unique
    private boolean tfg$hasTuffRings;
    @Unique
    private boolean tfg$hasTuyas;
    @Unique
    private int tfg$tuffRingFrequency;
    @Unique
    private int tfg$tuyaFrequency;
    @Unique
    private int tfg$shoreBaseHeight;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void tfg$init(CallbackInfo ci) {
        tfg$shoreBlendType = ShoreBlendType.NONE;

        tfg$hasTuffRings = false;
        tfg$hasTuyas = false;

        tfg$tuffRingFrequency = 0;
        tfg$tuyaFrequency = 0;

        tfg$shoreBaseHeight = TFCChunkGenerator.SEA_LEVEL_Y;
    }

    public BiomeBuilder tfg$type(ShoreBlendType type) {
        this.tfg$shoreBlendType = type;
        return (BiomeBuilder) (Object) this;
    }

    public BiomeBuilder tfg$tuffRings(int frequency, int baseHeight, int scaleHeight) {
        this.tfg$hasTuffRings = true;
        this.tfg$tuffRingFrequency = frequency;

        assert heightNoiseFactory != null : "tuff rings must be called after setting a heightmap";
        assert surfaceBuilderFactory != null : "volcanoes must be called after setting a surface builder";

        final LongFunction<Noise2D> baseHeightNoiseFactory = this.heightNoiseFactory;
        this.heightNoiseFactory = seed -> TFGBiomeNoise.addTuffRings(Seed.of(seed), baseHeightNoiseFactory.apply(seed), frequency, baseHeight, scaleHeight);
        this.noiseFactory = seed -> BiomeNoiseSampler.fromHeightNoise(heightNoiseFactory.apply(seed));

        this.surfaceBuilderFactory = TuffRingsSurfaceBuilder.create(surfaceBuilderFactory);

        return (BiomeBuilder) (Object) this;
    }

    public BiomeBuilder tfg$tuyas(int frequency, int baseHeight, int scaleHeight, int volcanoBasaltHeight, boolean icy) {
        this.tfg$hasTuyas = true;
        this.tfg$tuyaFrequency = frequency;
        this.volcanoBasaltHeight = SEA_LEVEL_Y + volcanoBasaltHeight;

        assert heightNoiseFactory != null : "volcanoes must be called after setting a heightmap";
        assert surfaceBuilderFactory != null : "volcanoes must be called after setting a surface builder";

        final LongFunction<Noise2D> baseHeightNoiseFactory = this.heightNoiseFactory;
        this.heightNoiseFactory = seed -> TFGBiomeNoise.addTuyas(Seed.of(seed), baseHeightNoiseFactory.apply(seed), frequency, baseHeight, scaleHeight, icy);
        this.noiseFactory = seed -> BiomeNoiseSampler.fromHeightNoise(heightNoiseFactory.apply(seed));

        this.surfaceBuilderFactory = TuyasSurfaceBuilder.create(surfaceBuilderFactory);

        return (BiomeBuilder) (Object) this;
    }

    public BiomeBuilder tfg$volcanoes(int frequency, int baseHeight, int scaleHeight, int volcanoBasaltHeight) {
        return tfg$volcanoes(frequency, baseHeight, scaleHeight, volcanoBasaltHeight, false);
    }

    public BiomeBuilder tfg$volcanoes(int frequency, int baseHeight, int scaleHeight, int volcanoBasaltHeight, boolean additive) {
        this.volcanic = true;
        this.volcanoFrequency = frequency;
        this.volcanoBasaltHeight = SEA_LEVEL_Y + volcanoBasaltHeight;

        assert heightNoiseFactory != null : "volcanoes must be called after setting a heightmap";
        assert surfaceBuilderFactory != null : "volcanoes must be called after setting a surface builder";

        final LongFunction<Noise2D> baseHeightNoiseFactory = this.heightNoiseFactory;
        this.heightNoiseFactory = seed -> TFGBiomeNoise.addVolcanoes(Seed.of(seed), baseHeightNoiseFactory.apply(seed), frequency, baseHeight, scaleHeight, additive);

        this.noiseFactory = seed -> BiomeNoiseSampler.fromHeightNoise(heightNoiseFactory.apply(seed));

        this.surfaceBuilderFactory = VolcanoesSurfaceBuilder.create(surfaceBuilderFactory);

        return (BiomeBuilder) (Object) this;
    }

    @Inject(method = "build", at = @At("RETURN"), remap = false, cancellable = true)
    public void tfg$build(ResourceKey<Biome> key, CallbackInfoReturnable<BiomeExtension> cir) {
        var extension = cir.getReturnValue();
        ((IBiomeExtension) extension).tfg$init(tfg$shoreBlendType, tfg$hasTuffRings, tfg$hasTuyas, tfg$tuffRingFrequency, tfg$tuyaFrequency, tfg$shoreBaseHeight);
        cir.setReturnValue(extension);
    }
}
