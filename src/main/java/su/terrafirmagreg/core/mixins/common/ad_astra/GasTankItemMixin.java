package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.item.ItemStack;

import earth.terrarium.adastra.common.items.GasTankItem;
import earth.terrarium.adastra.common.registry.ModItems;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = GasTankItem.class, remap = false)
public abstract class GasTankItemMixin {

    /**
     * @author Pyritie
     * @reason Fluid tank is too small
     */
    @Overwrite()
    public WrappedItemFluidContainer getFluidContainer(ItemStack holder) {

        return new WrappedItemFluidContainer(
                holder,
                new SimpleFluidContainer(
                        FluidConstants.fromMillibuckets(holder.getItem() == ModItems.GAS_TANK.get() ? 4000 : 8000),
                        1,
                        (t, f) -> f.is(TFGTags.Fluids.BreathableCompressedAir)));
    }
}
