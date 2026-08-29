package su.terrafirmagreg.core.mixins.common.tfc.items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.dries007.tfc.common.items.BlowpipeItem;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.utils.TFGHelpers;

@Mixin(value = BlowpipeItem.class)
public abstract class BlowpipeItemMixin {

    /**
     * @author Redeix
     * @reason Replace hardcoded use duration with TFG dynamic duration.
     */
    @Overwrite
    public int getUseDuration(ItemStack stack) {
        return TFGHelpers.getGlassworkingStat(null, false);
    }
}
