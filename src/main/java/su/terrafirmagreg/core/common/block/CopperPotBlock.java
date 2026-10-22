package su.terrafirmagreg.core.common.block;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CopperPotBlock extends CookingApplianceBlock {

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 1.0D, 14.0D),
            Block.box(2.0D, 1.0D, 2.0D, 14.0D, 14.0D, 3.0D),
            Block.box(2.0D, 1.0D, 13.0D, 14.0D, 14.0D, 14.0D),
            Block.box(2.0D, 1.0D, 3.0D, 3.0D, 14.0D, 13.0D),
            Block.box(13.0D, 1.0D, 3.0D, 14.0D, 14.0D, 13.0D));

    public CopperPotBlock(ExtendedProperties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
