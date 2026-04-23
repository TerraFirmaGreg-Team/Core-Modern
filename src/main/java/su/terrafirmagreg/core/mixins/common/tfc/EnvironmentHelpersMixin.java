package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import earth.terrarium.adastra.api.planets.PlanetApi;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;

@Mixin(value = EnvironmentHelpers.class, remap = false)
public abstract class EnvironmentHelpersMixin {

    /**
     * Stops TFC from trying to melt/freeze water on other planets, so ad astra can handle it instead
     */

    @Inject(method = "tickChunk", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$tickChunk(ServerLevel level, LevelChunk chunk, ProfilerFiller profiler, CallbackInfo ci) {
        if (PlanetApi.API.isExtraterrestrial(level) && !Level.OVERWORLD.equals(level.dimension())) {
            ci.cancel();
        }
    }

    /**
     * Adds mars water as a water block
     */

    @Inject(method = "isWater", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$isWater(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (Helpers.isBlock(state, TFGBlocks.MARS_WATER.get())) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Prevents icicles from forming underneath blocks with 'tfg:no_icicle_generation' block tag
     */

    @ModifyExpressionValue(method = "doIcicles", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/Helpers;isBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean tfg$modifyTagCheck(boolean original, @Local(name = "stateAbove") BlockState stateAbove) {
        return original || Helpers.isBlock(stateAbove, TFGTags.Blocks.NoIcicles);
    }
}
