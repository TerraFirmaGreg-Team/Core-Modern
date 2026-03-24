package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;

import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.ISurfaceBuilderContext;

@Mixin(value = SurfaceBuilderContext.class, remap = false)
public class SurfaceBuilderContextMixin implements ISurfaceBuilderContext {

    @Unique
    private BiomeExtension tfg$cinderConeBiome;
    @Unique
    private BiomeExtension tfg$tuffRingBiome;
    @Unique
    private BiomeExtension tfg$tuyaBiome;

    @Override
    public void tfg$init(BiomeExtension cinderConeBiome, BiomeExtension tuffRingBiome, BiomeExtension tuyaBiome) {
        tfg$cinderConeBiome = cinderConeBiome;
        tfg$tuffRingBiome = tuffRingBiome;
        tfg$tuyaBiome = tuyaBiome;
    }

    @Override
    public BiomeExtension tfg$getCinderConeBiome() {
        return tfg$cinderConeBiome;
    }

    @Override
    public BiomeExtension tfg$getTuffRingBiome() {
        return tfg$tuffRingBiome;
    }

    @Override
    public BiomeExtension tfg$getTuyaBiome() {
        return tfg$tuyaBiome;
    }
}
