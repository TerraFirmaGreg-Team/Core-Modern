package su.terrafirmagreg.core.mixins.common.ad_astra;

import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.adastra.common.registry.ModItems;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = SpaceSuitItem.class, remap = false)
public abstract class SpaceSuitItemMixin {
	/**
	 * @author Pyritie
	 * @reason Change to our own fluid tags
	 */
	@Overwrite()
	public WrappedItemFluidContainer getFluidContainer(ItemStack holder) {
		int size;
		Item item = holder.getItem();

		if (item == ModItems.SPACE_SUIT.get())
			size = 1000;
		else if (item == ModItems.NETHERITE_SPACE_SUIT.get())
			size = 2000;
		else
			size = 4000;

		return new WrappedItemFluidContainer(
			holder,
			new SimpleFluidContainer(
				FluidConstants.fromMillibuckets(size),
				1,
				(t, f) -> f.is(TFGTags.Fluids.BreathableCompressedAir)));
	}

	// For some reason, trying to overwrite or inject into SpaceSuitItem.appendHoverText doesn't work,
	// which means we can't really change the fluid capacity either
}
