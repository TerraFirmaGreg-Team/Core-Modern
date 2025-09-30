package su.terrafirmagreg.core.mixins.common.beneath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.eerussianguy.beneath.world.feature.NetherSpikesFeature;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.common.data.TFGBlockProperties;

@Mixin(value = NetherSpikesFeature.class, remap = false)
public class NetherSpikesFeatureMixin {

    /**
     * @author Pyritie
     * @reason Change the normal rock spike block fluid property to the TFG one to support more fluids
     */
    @Overwrite
    protected void replaceBlock(WorldGenLevel level, BlockPos pos, BlockState state) {
        final Block block = level.getBlockState(pos).getBlock();
        if (block == Blocks.AIR) {
            level.setBlock(pos, state, 3);
        } else if (block != Blocks.WATER && block != TFCBlocks.RIVER_WATER.get()) {
            if (block == Blocks.LAVA) {
                level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(Fluids.LAVA)), 3);
            }
        } else {
            level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(Fluids.WATER)), 3);
        }
    }
}
