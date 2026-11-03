package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;

import net.dries007.tfc.common.fluids.FluidPropertyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = Contraption.class, remap = false)
public class ContraptionMixin {

    // Writes a message in chat to nearby players when a contraption is formed in the beneath or on hot planets.
    // Yes, this means it'll also announce to players setting up a tree farm or something. It's fine!

    @Inject(method = "onEntityCreated", at = @At("TAIL"), remap = false)
    private void tfg$onEntityCreated(AbstractContraptionEntity entity, CallbackInfo ci) {
        var level = entity.level();
        var server = level.getServer();
        if (server == null)
            return;

        var dim = level.dimension();
        if (server.isSingleplayer()) {
            if (dim == Level.NETHER && TFGConfig.SERVER.enableBeneathMiningRestrictions.get()) {

                final int y = TFGConfig.SERVER.disabledBeneathMiningYLevel.get();
                server.sendSystemMessage(Component.translatable("tfg.clientmessage.nether_contraption_created", y));

            } else if ((dim == Planet.VENUS || dim == Planet.MERCURY) && TFGConfig.SERVER.enableHotPlanetMiningRestrictions.get()) {

                server.sendSystemMessage(Component.translatable("tfg.clientmessage.hot_planet_contraption_created"));
            }
        } else {
            for (var player : server.getPlayerList().getPlayers()) {
                if (player.position().distanceTo(entity.position()) < 20) {
                    if (dim == Level.NETHER && TFGConfig.SERVER.enableBeneathMiningRestrictions.get()) {

                        final int y = TFGConfig.SERVER.disabledBeneathMiningYLevel.get();
                        player.sendSystemMessage(Component.translatable("tfg.clientmessage.nether_contraption_created", y));

                    } else if ((dim == Planet.VENUS || dim == Planet.MERCURY) && TFGConfig.SERVER.enableHotPlanetMiningRestrictions.get()) {

                        player.sendSystemMessage(Component.translatable("tfg.clientmessage.hot_planet_contraption_created"));
                    }
                }
            }
        }
    }

    // Stops contraptions from messing with TFC fluid block properties in weird ways
    // (Sent to me by Allmoz from ATFC)

    @WrapOperation(method = "removeBlocksFromWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 1, remap = true))
    private boolean tfg$preserveTFCFluidState(Level world, BlockPos pPos, BlockState pNewState, int pFlags, Operation<Boolean> original, @Local BlockState oldState) {
        if (FluidPropertyHelper.hasProperty(oldState)) {
            return world.setBlock(pPos, FluidPropertyHelper.getFluidBlockState(oldState), pFlags);
        }
        return original.call(world, pPos, pNewState, pFlags);
    }

    @WrapOperation(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z", remap = true, ordinal = 0))
    private boolean tfg$rememberTFCFluidState(Level world, BlockPos pos, boolean dropBlock, Operation<Boolean> original, @Share("oldFluidState") LocalRef<FluidState> oldFluidState) {
        oldFluidState.set(world.getFluidState(pos));
        return original.call(world, pos, dropBlock);
    }

    @WrapOperation(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", remap = true))
    private boolean tfg$applyTFCFluidState(Level world, BlockPos pPos, BlockState pNewState, int pFlags, Operation<Boolean> original, @Share("oldFluidState") LocalRef<FluidState> oldFluidState) {
        if (FluidPropertyHelper.hasProperty(pNewState)) {
            pNewState = FluidPropertyHelper.set(pNewState, oldFluidState.get().getType());
        }
        return original.call(world, pPos, pNewState, pFlags);
    }
}
