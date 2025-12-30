package su.terrafirmagreg.core.mixins.common.firmalife;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.firmalife.common.blockentities.LargePlanterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.systems.OxygenApi;

@Mixin(LargePlanterBlockEntity.class)
public abstract class LargePlanterBlockEntityOxygenMixin {

    @Unique
    private boolean tfg$hasAdjacentOxygen = true;

    @Unique
    private static boolean tfg$computeHasAdjacentOxygen(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (OxygenApi.API.hasOxygen(level, pos.relative(dir))) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "onCalendarUpdate", at = @At("HEAD"), remap = false)
    private void tfg$updateOxygenCacheOnCalendarUpdate(long ticks, CallbackInfo ci) {
        LargePlanterBlockEntity self = (LargePlanterBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (!(level instanceof ServerLevel serverLevel))
            return;

        boolean hasOxygenNow = tfg$computeHasAdjacentOxygen(serverLevel, self.getBlockPos());
        if (tfg$hasAdjacentOxygen != hasOxygenNow) {
            tfg$hasAdjacentOxygen = hasOxygenNow;
            self.markForSync();
        }
    }

    @Inject(method = "getInvalidReason", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$injectOxygenInvalidReason(CallbackInfoReturnable<@Nullable Component> cir) {
        if (!tfg$hasAdjacentOxygen) {
            cir.setReturnValue(Component.translatable("tfg.tooltip.firmalife_greenhouse.oxygen_required"));
        }
    }

    @Inject(method = "afterGrowthTickStep", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$blockGrowthWhenNoOxygen(boolean wasGrowing, CallbackInfo ci) {
        if (!tfg$hasAdjacentOxygen) {
            ci.cancel();
        }
    }
}
