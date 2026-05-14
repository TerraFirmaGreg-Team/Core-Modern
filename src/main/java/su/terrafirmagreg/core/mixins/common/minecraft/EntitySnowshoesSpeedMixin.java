package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import su.terrafirmagreg.core.common.event.WearableAccessoryHandler;

/**
 * Snowshoes in {@link net.minecraft.world.entity.EquipmentSlot#FEET}: on tagged blocks, cancel block speed slowdown (tfccanes-style).
 */
@Mixin(Entity.class)
public class EntitySnowshoesSpeedMixin {

    @Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), cancellable = true)
    private void tfg$snowshoesBlockSpeed(CallbackInfoReturnable<Float> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        float factor = cir.getReturnValue();
        if (factor >= 1.0F) {
            return;
        }
        if (!WearableAccessoryHandler.isFeetInSnowshoesTaggedBlock(self)) {
            return;
        }
        if (!WearableAccessoryHandler.hasSnowshoesEquipped(player)) {
            return;
        }
        cir.setReturnValue(1.0F);
    }
}
