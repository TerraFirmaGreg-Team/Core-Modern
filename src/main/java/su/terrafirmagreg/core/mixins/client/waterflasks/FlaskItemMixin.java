package su.terrafirmagreg.core.mixins.client.waterflasks;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.items.DiscreteFluidContainerItem;
import net.dries007.tfc.util.Drinkable;
import net.dries007.tfc.util.Helpers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.labellum.mc.waterflasks.item.FlaskItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(value = FlaskItem.class, remap = false)
public abstract class FlaskItemMixin extends DiscreteFluidContainerItem {

    public FlaskItemMixin(Properties properties, Supplier<Integer> capacity, TagKey<Fluid> whitelist, boolean canPlaceLiquidsInWorld, boolean canPlaceSourceBlocks) {
        super(properties, capacity, whitelist, canPlaceLiquidsInWorld, canPlaceSourceBlocks);
    }

    /**
     * @author Ujhik
     * @reason Add tfc behavior enabling certain drinks like wines that give buffs to be drink when player has full water meter
     */
    @Inject(method = "use", at = @At(value = "RETURN"), cancellable = true, remap = true)
    private void tfg$useMayDrinkWhenFull(Level level, Player player, InteractionHand usedHand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

        ItemStack stack = player.getItemInHand(usedHand);
        final IFluidHandler handler = Helpers.getCapability(stack, Capabilities.FLUID_ITEM);
        var result = cir.getReturnValue().getResult();
        if (handler != null && result == InteractionResult.FAIL) {
            final Drinkable drinkable = Drinkable.get(handler.getFluidInTank(0).getFluid());

            if (drinkable != null && drinkable.mayDrinkWhenFull())
                cir.setReturnValue(afterEmptyFailed(handler, level, player, stack, usedHand));
        }
    }
}
