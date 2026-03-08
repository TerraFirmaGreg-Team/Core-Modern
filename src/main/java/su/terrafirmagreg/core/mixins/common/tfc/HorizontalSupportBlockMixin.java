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
import net.minecraft.server.level.ServerLevel;
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
        if (!(level instanceof ServerLevel serverLevel))
            return;
        Support support = Support.get(state);
        if (support == null)
            return;
        SupportCache.forLevel(serverLevel).addSupport(pos.immutable(), support);
    }

    /**
     * Add the automatically extended beam blocks to the cache too.
     */
    @WrapOperation(method = "setPlacedBy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean tfg$cacheSetBlock(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        boolean result = original.call(level, pos, state, flags);
        if (result && level instanceof ServerLevel serverLevel) {
            Support support = Support.get(state);
            if (support != null) {
                SupportCache.forLevel(serverLevel).addSupport(pos.immutable(), support);
            }
        }
        return result;
    }

    @SuppressWarnings({ "NullableProblems", "deprecation" })
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (level instanceof ServerLevel serverLevel && state.getBlock() != newState.getBlock()) {
            SupportCache.forLevel(serverLevel).removeSupport(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
