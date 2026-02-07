package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

@Mixin(value = TFCBiomes.class, remap = false)
public interface TFCBiomesAccessor {

    @Accessor("EXTENSIONS")
    static Map<ResourceKey<Biome>, BiomeExtension> getExtensions() {
        throw new AssertionError();
    }
}
