package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.dries007.tfc.ForgeEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;

@Mixin(value = ForgeEventHandler.class)
public class ForgeEventHandlerMixin {

    // Forcibly disable nether portals because there's some funky mod conflict going on with
    // settings overwriting each other

    @Inject(method = "onCreateNetherPortal", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$onCreateNetherPortal(BlockEvent.PortalSpawnEvent event, CallbackInfo ci) {
        event.setCanceled(true);
        ci.cancel();
    }

    // Don't create water source blocks when hot items melt the ice
    // target = ServerLevel.setBlockAndUpdate(BlockPos, BlockState)
    // There's a few of those in the method so have to check which blocks are being melted.
    @WrapOperation(method = "onItemExpire", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", remap = true), remap = false)
    private static boolean tfg$preventHotIceWater(ServerLevel level, BlockPos pos, BlockState newState, Operation<Boolean> original) {
        Block currentBlock = level.getBlockState(pos).getBlock();

        // Only intercept packed/blue ice melting
        if ((currentBlock == Blocks.PACKED_ICE || currentBlock == Blocks.BLUE_ICE) && newState.is(Blocks.WATER)) {
            level.destroyBlock(pos, false);
            return true;
        }

        // Otherwise do as usual
        return original.call(level, pos, newState);
    }
}
