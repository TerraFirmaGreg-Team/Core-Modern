package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.simibubi.create.content.kinetics.base.IRotate;

import net.dries007.tfc.common.blocks.devices.BellowsBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;
import electrolyte.greate.registry.GreateMaterials;

@Mixin(value = BellowsBlock.class, remap = false)
public abstract class BellowsBlockMixin implements IRotate, ITieredBlock {
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(BellowsBlock.FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(BellowsBlock.FACING).getAxis();
    }

    @Override
    public int getTier() {
        return 0;
    }

    @Override
    public void setTier(int i) {
    }

    @Override
    public Material getMaterial() {
        return GreateMaterials.AndesiteAlloy;
    }
}
