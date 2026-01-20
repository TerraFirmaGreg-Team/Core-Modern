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

/**
 * Enforces oxygen adjacency for Firmalife's planters (which all extend the large planter class).
 * <p>
 * <p>Behavior:
 * <p>- Checks the adjacent positions for oxygen availability via ad_astra's OxygenApi.
 * <p>- Syncs the block entity when the cached oxygen state changes.
 * <p>- Prevents growth ticks when oxygen is missing.
 */
@Mixin(LargePlanterBlockEntity.class)
public abstract class LargePlanterBlockEntityOxygenMixin {

    @Unique
    private boolean tfg$hasAdjacentOxygen = true;

    /**
     * Check if any adjacent position has oxygen.
     *
     * @param level server level used for the oxygen API.
     * @param pos   block position to test around.
     * @return true if any of the 6 faces have oxygen.
     */
    @Unique
    private static boolean tfg$computeHasAdjacentOxygen(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (OxygenApi.API.hasOxygen(level, pos.relative(dir))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the cached oxygen adjacency on calendar ticks.
     * If the state changes, marks the block entity for sync.
     *
     * @param ticks calendar ticks from Firmalife.
     * @param ci    callback info.
     */
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

    /**
     * Injects an invalid reason when oxygen is missing.
     *
     * @param cir callback info with component.
     */
    @Inject(method = "getInvalidReason", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$injectOxygenInvalidReason(CallbackInfoReturnable<@Nullable Component> cir) {
        if (!tfg$hasAdjacentOxygen) {
            cir.setReturnValue(Component.translatable("tfg.tooltip.firmalife_greenhouse.oxygen_required"));
        }
    }

    /**
     * Blocks growth tick steps when the planter lacks oxygen.
     *
     * @param wasGrowing whether growth was progressing.
     * @param ci         callback info.
     */
    @Inject(method = "afterGrowthTickStep", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$blockGrowthWhenNoOxygen(boolean wasGrowing, CallbackInfo ci) {
        if (!tfg$hasAdjacentOxygen) {
            ci.cancel();
        }
    }
}
