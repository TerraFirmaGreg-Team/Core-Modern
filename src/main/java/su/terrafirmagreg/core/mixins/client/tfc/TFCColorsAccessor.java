package su.terrafirmagreg.core.mixins.client.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.dries007.tfc.client.TFCColors;

/**
 * Accessor for TFCColors.
 */
@Mixin(value = TFCColors.class, remap = false)
public interface TFCColorsAccessor {

    @Accessor("FOLIAGE_COLORS_CACHE")
    static int[] getFoliageColorsCache() {
        throw new AssertionError();
    }
}
