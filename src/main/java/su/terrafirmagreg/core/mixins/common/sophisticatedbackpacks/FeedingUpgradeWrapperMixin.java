package su.terrafirmagreg.core.mixins.common.sophisticatedbackpacks;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

// Cancel backpack feeding upgrade tick when player is in PlayerRevive bleeding state
// This prevents food from being consumed without effect

@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper", remap = false)
public class FeedingUpgradeWrapperMixin {
    @Inject(method = "tick", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$preventFeedingWhenBleeding(@Nullable Entity entity, Level level, BlockPos pos, CallbackInfo ci) {
        if (entity instanceof Player player && player.getPersistentData().getBoolean("playerrevive:bleeding")) {
            ci.cancel();
        }
    }
}