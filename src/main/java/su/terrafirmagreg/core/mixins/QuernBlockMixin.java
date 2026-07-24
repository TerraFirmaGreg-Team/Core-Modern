package su.terrafirmagreg.core.mixins;

import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.simibubi.create.content.kinetics.base.IRotate;

import net.dries007.tfc.common.blocks.devices.QuernBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;
import electrolyte.greate.registry.GreateMaterials;

@Mixin(value = QuernBlock.class, remap = false)
public abstract class QuernBlockMixin implements IRotate, ITieredBlock {
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
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
