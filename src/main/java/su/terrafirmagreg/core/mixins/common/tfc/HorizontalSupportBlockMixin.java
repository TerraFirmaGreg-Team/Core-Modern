package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.wood.HorizontalSupportBlock;
import net.dries007.tfc.common.blocks.wood.VerticalSupportBlock;
import net.dries007.tfc.util.Support;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.perf.SupportCache;

@Mixin(value = HorizontalSupportBlock.class, remap = true)
public abstract class HorizontalSupportBlockMixin extends VerticalSupportBlock {

    protected HorizontalSupportBlockMixin(ExtendedProperties properties) {
        super(properties);
    }

    /**
     * Add the directly-placed beam block at pos to the cache.
     */
    @Inject(method = "setPlacedBy", at = @At("HEAD"))
    private void tfg$cachePlacedPos(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        Support support = Support.get(state);
        if (support == null)
            return;
        SupportCache.forLevel(level).addSupport(pos.immutable(), support);
    }

    /**
     * Add the automatically extended beam blocks to the cache too.
     */
    @WrapOperation(method = "setPlacedBy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean tfg$cacheSetBlock(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        boolean result = original.call(level, pos, state, flags);
        if (result) {
            Support support = Support.get(state);
            if (support != null) {
                SupportCache.forLevel(level).addSupport(pos.immutable(), support);
            }
        }
        return result;
    }

    /**
     * Unfortunately this is only called ServerSide, so the clientside cache may contain stale supports.
     * In practice this is no problem because we doublecheck all found supports and remove them if they're stale,
     * so clientside the cache is just a little lazier about removal.
     */
    @SuppressWarnings({ "NullableProblems", "deprecation" })
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            SupportCache.forLevel(level).removeSupport(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
