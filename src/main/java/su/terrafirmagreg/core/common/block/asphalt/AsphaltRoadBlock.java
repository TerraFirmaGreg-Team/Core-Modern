package su.terrafirmagreg.core.common.block.asphalt;

import com.therighthon.rnr.RNRHelpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class AsphaltRoadBlock extends Block {

    /** Same as {@code com.therighthon.rnr.common.block.PathHeightBlock}: matches {@code rnr:block/path_block} height (one voxel short). */
    protected static final VoxelShape PATH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public AsphaltRoadBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(AsphaltRoadHelper.MASK, AsphaltRoadMarkingMask.NONE)
                .setValue(AsphaltRoadHelper.FACING, Direction.NORTH)
                .setValue(AsphaltRoadHelper.COLOR, DyeColor.WHITE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AsphaltRoadHelper.MASK, AsphaltRoadHelper.FACING, AsphaltRoadHelper.COLOR);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PATH_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PATH_SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return PATH_SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = RNRHelpers.blockModRecipeCompatible(state, level, pos, player, hand, hit);
        return result == InteractionResult.FAIL ? InteractionResult.PASS : result;
    }

}
