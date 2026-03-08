package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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

    /** Check which hand the used gas tank is in and update that hand. */
    @Redirect(method = "onUseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setItem(ILnet/minecraft/world/item/ItemStack;)V"), remap = true)
    private void fixOffHandTankWrite(Inventory inventory, int index, ItemStack updatedStack, @Local Player player) {
        if (player.getUsedItemHand() == InteractionHand.OFF_HAND) {
            inventory.offhand.set(0, updatedStack);
        } else {
            inventory.setItem(index, updatedStack);
        }
    }
}
