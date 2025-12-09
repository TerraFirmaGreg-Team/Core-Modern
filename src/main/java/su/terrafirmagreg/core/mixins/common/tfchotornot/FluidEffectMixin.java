package su.terrafirmagreg.core.mixins.common.tfchotornot;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.TFCEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import tfchotornot.EventHandler;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = EventHandler.FluidEffect.class, remap = false)
public class FluidEffectMixin {

    // If the player is wearing diving boots or is burdened, cancel

    @Inject(method = "lambda$static$3", at = @At(value = "HEAD", target = "Ltfchotornot/EventHandler$FluidEffect;GAS:Ltfchotornot/EventHandler$FluidEffect;"), remap = false, cancellable = true)
    private static void tfg$gaseousLambda2(Player player, Level level, CallbackInfo ci) {
        ItemStack item = player.getItemBySlot(EquipmentSlot.FEET);
        if (item.is(TFGTags.Items.PreventsGasFloating)) {
            ci.cancel();
            return;
        }

        if (player.hasEffect(TFCEffects.OVERBURDENED.get()) || player.hasEffect(TFCEffects.EXHAUSTED.get())) {
            ci.cancel();
            return;
        }

        // Increase levitation duration, by default it's 10-25 ticks, which means the player never stays airborne
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));

        ci.cancel();
    }
}
