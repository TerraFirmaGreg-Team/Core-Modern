package su.terrafirmagreg.core.mixins.common.tfchotornot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.TFCEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

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

    // The first thing the mod does is check if the player has fire prot or resistance... which this should nullify.

    @Redirect(method = "onPlayerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"), remap = true)
    private static boolean tfg$onPlayerTick(Player instance, MobEffect mobEffect) {
        return false;
    }

    // Check for equipment before managing any items or yeeting

    @Inject(method = "applyEffects(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Ltfchotornot/EventHandler$FluidEffect;Lnet/minecraft/world/level/Level;)V", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$applyEffects(ItemStack stack, Player player, EventHandler.FluidEffect effect, Level level, CallbackInfo ci) {
        if (effect == EventHandler.FluidEffect.HOT) {
            if (player.getItemBySlot(EquipmentSlot.CHEST).is(TFGTags.Items.HotProtectionEquipment))
                ci.cancel();
            else if (player.hasEffect(MobEffects.FIRE_RESISTANCE))
                ci.cancel();
        } else if (effect == EventHandler.FluidEffect.COLD) {
            if (player.getItemBySlot(EquipmentSlot.CHEST).is(TFGTags.Items.ColdProtectionEquipment))
                ci.cancel();
        } else if (effect == EventHandler.FluidEffect.GAS) {
            if (player.getItemBySlot(EquipmentSlot.FEET).is(TFGTags.Items.FloatingProtectionEquipment))
                ci.cancel();
            else if (player.hasEffect(TFCEffects.OVERBURDENED.get()) || player.hasEffect(TFCEffects.EXHAUSTED.get()))
                ci.cancel();
            else {
                // Increase levitation duration, by default it's 10-25 ticks, which means the player never stays airborne
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
                ci.cancel();
            }
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
