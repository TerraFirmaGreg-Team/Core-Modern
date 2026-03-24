package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_VERSION;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeBuilder;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.world.new_ow_wg.biome.IBiomeBuilder;
import su.terrafirmagreg.core.world.new_ow_wg.biome.IBiomeExtension;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.CinderConeSurfaceBuilder;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.TuffRingsSurfaceBuilder;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.TuyaSurfaceBuilder;

/**
 * Adds additional data to biome builders that are new in 1.21
 */

@Mixin(value = BiomeBuilder.class, remap = false)
public class BiomeBuilderMixin implements IBiomeBuilder {
    @Shadow
    private SurfaceBuilderFactory surfaceBuilderFactory;
    @Shadow
    private boolean sandyRiverShores;

    @Unique
    private TFGRiverBlendType tfg$riverBlendType;
    @Unique
    private ShoreBlendType tfg$shoreBlendType;
    @Unique
    private int tfg$shoreBaseHeight;
    @Unique
    private boolean tfg$hasCinderCones;
    @Unique
    private boolean tfg$hasTuffRings;
    @Unique
    private boolean tfg$hasTuyas;
    @Unique
    private boolean tfg$centeredFeatureIce;
    @Unique
    private int tfg$centeredFeatureFrequency;
    @Unique
    private int tfg$centeredFeatureRockHeight;
    @Unique
    private int tfg$centeredFeatureBaseHeight;
    @Unique
    private int tfg$centeredFeatureScaleHeight;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void tfg$init(CallbackInfo ci) {
        tfg$shoreBlendType = ShoreBlendType.NONE;
        tfg$riverBlendType = TFGRiverBlendType.NONE;
        tfg$shoreBaseHeight = TFCChunkGenerator.SEA_LEVEL_Y;

        tfg$hasCinderCones = false;
        tfg$hasTuffRings = false;
        tfg$hasTuyas = false;

        tfg$centeredFeatureIce = false;
        tfg$centeredFeatureFrequency = 0;
        tfg$centeredFeatureRockHeight = 0;
        tfg$centeredFeatureBaseHeight = 0;
        tfg$centeredFeatureScaleHeight = 0;
    }

    public BiomeBuilder tfg$type(ShoreBlendType type) {
        this.tfg$shoreBlendType = type;
        return (BiomeBuilder) (Object) this;
    }

    public BiomeBuilder tfg$type(TFGRiverBlendType type) {
        this.tfg$riverBlendType = type;
        if (type == TFGRiverBlendType.CAVE)
            this.sandyRiverShores = false;
        return (BiomeBuilder) (Object) this;
    }

    public BiomeBuilder tfg$setShoreBaseHeight(int height) {
        this.tfg$shoreBaseHeight = SEA_LEVEL_Y + height;
        return (BiomeBuilder) (Object) this;
    }

    @Inject(method = "surface", at = @At("TAIL"), remap = false, cancellable = true)
    private void tfg$surface(SurfaceBuilderFactory surfaceBuilderFactory, CallbackInfoReturnable<BiomeBuilder> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            this.surfaceBuilderFactory = CinderConeSurfaceBuilder.create(surfaceBuilderFactory);
            this.surfaceBuilderFactory = TuffRingsSurfaceBuilder.create(this.surfaceBuilderFactory);
            this.surfaceBuilderFactory = TuyaSurfaceBuilder.create(this.surfaceBuilderFactory);
            cir.setReturnValue((BiomeBuilder) (Object) this);
        }
    }

    public BiomeBuilder tfg$tuffRings(int frequency, int baseHeight, int scaleHeight) {
        this.tfg$hasTuffRings = true;
        this.tfg$centeredFeatureFrequency = frequency;
        this.tfg$centeredFeatureBaseHeight = baseHeight;
        this.tfg$centeredFeatureScaleHeight = scaleHeight;
        return (BiomeBuilder) (Object) this;
    }

    public BiomeBuilder tfg$tuyas(int frequency, int baseHeight, int scaleHeight, int tuyaBasaltHeight, boolean icy) {
        this.tfg$hasTuyas = true;
        this.tfg$centeredFeatureFrequency = frequency;
        this.tfg$centeredFeatureRockHeight = SEA_LEVEL_Y + tuyaBasaltHeight;
        this.tfg$centeredFeatureBaseHeight = baseHeight;
        this.tfg$centeredFeatureScaleHeight = scaleHeight;
        this.tfg$centeredFeatureIce = icy;
        return (BiomeBuilder) (Object) this;
    }

    public BiomeBuilder tfg$cinderCones(int frequency, int baseHeight, int scaleHeight, int cinderConeBasaltHeight, boolean additive) {
        this.tfg$hasCinderCones = true;
        this.tfg$centeredFeatureFrequency = frequency;
        this.tfg$centeredFeatureRockHeight = SEA_LEVEL_Y + cinderConeBasaltHeight;
        this.tfg$centeredFeatureBaseHeight = baseHeight;
        this.tfg$centeredFeatureScaleHeight = scaleHeight;
        return (BiomeBuilder) (Object) this;
    }

    @Inject(method = "build", at = @At("RETURN"), remap = false, cancellable = true)
    public void tfg$build(ResourceKey<Biome> key, CallbackInfoReturnable<BiomeExtension> cir) {
        var extension = cir.getReturnValue();
        ((IBiomeExtension) extension).tfg$init(tfg$shoreBlendType, tfg$riverBlendType, tfg$shoreBaseHeight, tfg$hasCinderCones, tfg$hasTuffRings, tfg$hasTuyas,
                tfg$centeredFeatureFrequency, tfg$centeredFeatureRockHeight, tfg$centeredFeatureBaseHeight, tfg$centeredFeatureScaleHeight, tfg$centeredFeatureIce);
        cir.setReturnValue(extension);
    }
}
