package su.terrafirmagreg.core.world.new_ow_wg.biome;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.biome.BiomeExtension;

public interface IBiomeBridge {
    @Nullable
    BiomeExtension tfg$getExtension(Supplier<BiomeExtension> init);
}
