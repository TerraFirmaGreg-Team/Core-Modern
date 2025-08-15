package su.terrafirmagreg.core.mixins.common.ad_astra;

import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.adastra.common.registry.ModItems;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = SpaceSuitItem.class, remap = false)
public abstract class SpaceSuitItemMixin {

	@Mutable
	@Shadow @Final
	protected long tankSize;

	@Inject(method = "<init>", at = @At(value = "TAIL", target = "Learth/terrarium/adastra/common/items/armor/SpaceSuitItem;<init>(L;L;JL;)V"), remap = false)
	private void tfg$SpaceSuitItem(ArmorMaterial material, ArmorItem.Type type, long tankSize, Item.Properties properties, CallbackInfo ci)
	{
		this.tankSize = (long) (tankSize * 2.5);
	}

	/**
	 * @author Pyritie
	 * @reason Change to our own fluid tags
	 */
	@Overwrite()
	public WrappedItemFluidContainer getFluidContainer(ItemStack holder) {
		return new WrappedItemFluidContainer(
			holder,
			new SimpleFluidContainer(
				FluidConstants.fromMillibuckets(tankSize),
				1,
				(t, f) -> f.is(TFGTags.Fluids.BreathableCompressedAir)));
	}
}
