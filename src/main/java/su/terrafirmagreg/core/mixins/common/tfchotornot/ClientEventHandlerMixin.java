package su.terrafirmagreg.core.mixins.common.tfchotornot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;

import tfchotornot.ClientEventHandler;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = ClientEventHandler.class, remap = false)
public class ClientEventHandlerMixin {

    @Inject(method = "applyEffectsFluid", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$applyEffectsFluid(ItemStack stack, FluidStack fluidStack, ItemTooltipEvent event, CallbackInfo ci) {
        if (stack.is(TFGTags.Items.InsulatingContainer)) {
            ci.cancel();
        }
    }
}
