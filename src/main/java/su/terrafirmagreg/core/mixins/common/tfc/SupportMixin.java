package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.util.Support;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.perf.SupportCache;

@Mixin(value = Support.class, remap = false)
public class SupportMixin {

    /**
     * Check our support cache to see if this position is supported by a HorizontalSupportBlock.
     */
    @Inject(method = "isSupported", at = @At("HEAD"), cancellable = true)
    private static void tfg$useSupportCache(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world instanceof Level level) {
            cir.setReturnValue(SupportCache.forLevel(level).isSupported(level, pos));
        }
    }

    /**
     * @author Mqrius
     * @reason Replace brute-force block scan with cache-based AABB intersection.
     */
    @Overwrite
    public static Set<BlockPos> findUnsupportedPositions(BlockGetter world, BlockPos from, BlockPos to) {
        if (world instanceof Level level) {
            return SupportCache.forLevel(level).findUnsupportedPositions(level, from, to);
        }
        return Set.of();
    }
}
