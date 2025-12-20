package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.utils.FoodStackingHelpers;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    // This mixin is needed for shift-clicking to stack properly
    @Inject(method = "isSameItemSameTags", at = @At("HEAD"), cancellable = true)
    private static void foodStacking(ItemStack a, ItemStack b, CallbackInfoReturnable<Boolean> cir) {
        Boolean result = FoodStackingHelpers.checkFoodStacking(a, b);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
