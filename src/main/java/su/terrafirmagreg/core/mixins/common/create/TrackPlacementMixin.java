package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.trains.track.TrackPlacement;

import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = TrackPlacement.class, remap = false)
public class TrackPlacementMixin {
    @WrapOperation(method = "placeTracks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canBeReplaced()Z", ordinal = 0), remap = true)
    private static boolean tfg$allowGroundElementsReplacement(BlockState state, Operation<Boolean> original) {
        return original.call(state) || state.is(TFGTags.Blocks.TRACK_REPLACEABLE);
    }
}
