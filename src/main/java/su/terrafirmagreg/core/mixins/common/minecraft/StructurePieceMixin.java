package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.blocks.wood.VerticalSupportBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

@Mixin(StructurePiece.class)
public class StructurePieceMixin {

    @Inject(method = "placeBlock", at = @At("TAIL"))
    private void tfg$postProcessSupports(WorldGenLevel level, BlockState blockstate, int x, int y, int z, BoundingBox boundingbox, CallbackInfo ci, @Local(name = "blockpos") BlockPos blockpos) {
        if (blockstate.getBlock() instanceof VerticalSupportBlock) {
            level.getChunk(blockpos).markPosForPostprocessing(blockpos);
        }
    }
}
