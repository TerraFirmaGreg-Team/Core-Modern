package su.terrafirmagreg.core.mixins.common.forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import su.terrafirmagreg.core.utils.FoodStackingHelpers;

@Mixin(value = ItemHandlerHelper.class, remap = false)
public class ItemHandlerHelperMixin {

    @Inject(method = "canItemStacksStack", at = @At("HEAD"), cancellable = true)
    private static void foodStacking(ItemStack a, ItemStack b, CallbackInfoReturnable<Boolean> cir) {
        Boolean result = FoodStackingHelpers.checkFoodStacking(a, b);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
