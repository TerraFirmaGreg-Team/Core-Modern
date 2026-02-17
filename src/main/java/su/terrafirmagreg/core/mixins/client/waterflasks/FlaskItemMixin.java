package su.terrafirmagreg.core.mixins.client.waterflasks;

import java.util.function.Supplier;

import org.labellum.mc.waterflasks.item.FlaskItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.items.DiscreteFluidContainerItem;
import net.dries007.tfc.util.Drinkable;
import net.dries007.tfc.util.Helpers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;

@Mixin(value = FlaskItem.class, remap = false)
public abstract class FlaskItemMixin extends DiscreteFluidContainerItem {

    public FlaskItemMixin(Properties properties, Supplier<Integer> capacity, TagKey<Fluid> whitelist, boolean canPlaceLiquidsInWorld, boolean canPlaceSourceBlocks) {
        super(properties, capacity, whitelist, canPlaceLiquidsInWorld, canPlaceSourceBlocks);
    }

    // spotless:off
    /**
     * @author Ujhik
     * @reason Add tfc behavior enabling certain drinks like wines that give buffs to be drink when player has full water meter
     */
    @ModifyExpressionValue(
            method = "use",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;getFoodData()Lnet/minecraft/world/food/FoodData;"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraftforge/fluids/FluidStack;getAmount()I",
                            remap = false
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;get()Ljava/lang/Object;",
                    ordinal = 0,
                    remap = false
            ),
            remap = true
    )
    private Object modifyConfig(Object original, @Local(name = "player") Player player, @Local(name = "hand") InteractionHand hand) {
        Boolean bOriginal = (Boolean) original;

        ItemStack stack = player.getItemInHand(hand);
        final IFluidHandler handler = Helpers.getCapability(stack, Capabilities.FLUID_ITEM);
        if (handler == null)
            return bOriginal;

        final Drinkable drinkable = Drinkable.get(handler.getFluidInTank(0).getFluid());
        if (drinkable == null)
            return bOriginal;

        return bOriginal || drinkable.mayDrinkWhenFull();
    }
    // spotless:on
}
