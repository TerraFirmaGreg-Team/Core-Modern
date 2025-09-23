package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.ItemStack;

@Mixin(targets = "net.dries007.tfc.common.blockentities.IngotPileBlockEntity$Entry", remap = false)
public interface IIngotPileBlockEntityEntryAccessor {

    @Accessor
    ItemStack getStack();
}
