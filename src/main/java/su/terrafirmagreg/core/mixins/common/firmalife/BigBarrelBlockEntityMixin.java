package su.terrafirmagreg.core.mixins.common.firmalife;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.firmalife.common.blockentities.BigBarrelBlockEntity;
import com.eerussianguy.firmalife.common.blocks.TwoByTwoBlock;

import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

@Mixin(value = BigBarrelBlockEntity.class, remap = false)
public abstract class BigBarrelBlockEntityMixin extends TFCBlockEntity {
    protected BigBarrelBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
    private void tfg$getCapability(Capability<?> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<?>> cir) {
        if (level == null) {
            return;
        }
        final BlockPos zeroPos = TwoByTwoBlock.findZeroPos(level, worldPosition, level.getBlockState(worldPosition));
        if (zeroPos.equals(worldPosition)) {
            return;
        }
        if (level.getBlockEntity(zeroPos) instanceof BigBarrelBlockEntity core) {
            cir.setReturnValue(core.getCapability(cap, side));
        } else {
            cir.setReturnValue(LazyOptional.empty()); // unloaded
        }
    }
}
