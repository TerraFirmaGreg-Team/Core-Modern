package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.PowderSnowBlock;

import su.terrafirmagreg.core.common.event.WearableAccessoryHandler;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockSnowshoesMixin {

    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void tfg$snowshoesCanWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && WearableAccessoryHandler.hasSnowshoesEquipped(player)) {
            cir.setReturnValue(true);
        }
    }
}
