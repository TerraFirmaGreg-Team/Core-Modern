package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.dries007.tfc.common.fluids.FluidPropertyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// Stops the schematicannon from placing waterlogged plants, allowing moving water sources
// (Sent to me by Allmoz from ATFC)

@Mixin(BlockHelper.class)
public abstract class BlockHelperMixin {

    @Inject(method = "placeSchematicBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/Blocks;COMPOSTER:Lnet/minecraft/world/level/block/Block;", ordinal = 0))
    private static void tfg$emptyTFCFluidState(Level world, BlockState state, BlockPos target, ItemStack stack, CompoundTag data, CallbackInfo ci,
            @Local(argsOnly = true) LocalRef<BlockState> stateRef) {
        BlockState blockState = stateRef.get();
        if (FluidPropertyHelper.hasProperty(blockState)) {
            stateRef.set(FluidPropertyHelper.empty(blockState));
        }
    }
}
