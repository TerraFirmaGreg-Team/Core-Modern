package su.terrafirmagreg.core.mixins.common.create.girders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.trains.track.TrackPaver;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.TFGTags;

/***
 * Credit: Create: More Girders
 */
@Mixin(value = TrackPaver.class)
public abstract class TrackPaverMixin {
    @Redirect(method = "paveCurve", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/track/TrackPaver;isWallLike(Lnet/minecraft/world/level/block/state/BlockState;)Z", remap = false), remap = false)
    private static boolean tfg$isWallLikeCurve(BlockState state) {
        if (state.is(TFGTags.Blocks.PAVING_GIRDER))
            return true;
        return state.getBlock() instanceof WallBlock || AllBlocks.METAL_GIRDER.has(state);
    }

    @Redirect(method = "paveStraight", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/track/TrackPaver;isWallLike(Lnet/minecraft/world/level/block/state/BlockState;)Z", remap = false), remap = false)
    private static boolean tfg$isWallLikeStraight(BlockState state) {
        if (state.is(TFGTags.Blocks.GIRDER))
            return true;
        return state.getBlock() instanceof WallBlock || AllBlocks.METAL_GIRDER.has(state);
    }

    @Redirect(method = "paveCurve", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/BlockEntry;has(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0, remap = false), remap = false)
    private static boolean tfg$isGirder(BlockEntry<?> entry, BlockState state) {
        if (state.is(TFGTags.Blocks.PAVING_GIRDER))
            return true;
        return entry.has(state);
    }

    @ModifyArg(method = "paveStraight", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/track/TrackPaver;placeBlockIfFree(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z", remap = false), index = 2, remap = false)
    private static BlockState tfg$setTopBracketForPaving(BlockState state) {
        if (state.is(TFGTags.Blocks.GIRDER)) {
            return state.setValue(GirderBlock.TOP, true);
        }
        return state;
    }

    @ModifyArg(method = "paveCurve", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/track/TrackPaver;placeBlockIfFree(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z", remap = false), index = 2, remap = false)
    private static BlockState tfg$setTopBracketForCurvePaving(BlockState state) {
        if (state.is(TFGTags.Blocks.GIRDER)) {
            return state.setValue(GirderBlock.TOP, true);
        }
        return state;
    }
}
