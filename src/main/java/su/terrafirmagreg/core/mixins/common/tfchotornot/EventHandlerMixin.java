package su.terrafirmagreg.core.mixins.common.tfchotornot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fluids.FluidStack;

import earth.terrarium.adastra.common.tags.ModItemTags;
import tfchotornot.EventHandler;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = EventHandler.class, remap = false)
public class EventHandlerMixin {

    // If the fluid is inside some sort of insulating container, cancel the effect

    @Inject(method = "applyEffectsFluid", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$applyEffectsFluid(ItemStack stack, FluidStack fluidStack, Player player, Level level, CallbackInfo ci) {
        if (stack.is(TFGTags.Items.InsulatingContainer)) {
            ci.cancel();
        }
    }

    // Cancel doing anything if the player is wearing a space suit

    @Inject(method = "onPlayerTick", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$onPlayerTick(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if (event.player.getItemBySlot(EquipmentSlot.CHEST).is(ModItemTags.SPACE_SUITS)) {
            ci.cancel();
        }
    }

    // Don't yeet gases

    @Inject(method = "yeetItem", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V", shift = At.Shift.AFTER), remap = false, cancellable = true)
    private static void tfg$yeetItem(ItemStack stack, Player player, EventHandler.FluidEffect effect, Level level, CallbackInfo ci) {
        if (effect == EventHandler.FluidEffect.GAS) {
            ci.cancel();
        }
    }
}
