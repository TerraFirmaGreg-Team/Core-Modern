package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;

import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = ChainConveyorConnectionHandler.class, remap = false)
public abstract class ChainConveyorConnectionHandlerMixin {

    @Inject(method = "isChain(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$isChain(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.is(TFGTags.Items.Chains)) {
            cir.setReturnValue(true);
        }
    }
}
