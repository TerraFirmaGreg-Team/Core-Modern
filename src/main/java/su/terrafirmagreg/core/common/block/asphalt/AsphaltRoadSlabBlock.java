package su.terrafirmagreg.core.common.block.asphalt;

import com.therighthon.rnr.RNRHelpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class AsphaltRoadSlabBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<AsphaltRoadMarkingColor> HORIZONTAL_COLOR = EnumProperty.create("horizontal_color", AsphaltRoadMarkingColor.class);
    public static final EnumProperty<AsphaltRoadMarkingColor> VERTICAL_COLOR = EnumProperty.create("vertical_color", AsphaltRoadMarkingColor.class);
    public static final EnumProperty<AsphaltRoadTopLayer> TOP_LAYER = EnumProperty.create("top_layer", AsphaltRoadTopLayer.class);
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);

    public AsphaltRoadSlabBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(WATERLOGGED, false)
                .setValue(HORIZONTAL_COLOR, AsphaltRoadMarkingColor.NONE)
                .setValue(VERTICAL_COLOR, AsphaltRoadMarkingColor.NONE)
                .setValue(TOP_LAYER, AsphaltRoadTopLayer.HORIZONTAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, HORIZONTAL_COLOR, VERTICAL_COLOR, TOP_LAYER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = RNRHelpers.blockModRecipeCompatible(state, level, pos, player, hand, hit);
        return result == InteractionResult.FAIL ? InteractionResult.PASS : result;
    }
}
