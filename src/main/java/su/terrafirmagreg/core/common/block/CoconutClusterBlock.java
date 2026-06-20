package su.terrafirmagreg.core.common.block;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import su.terrafirmagreg.core.common.data.PalmTrees;

@SuppressWarnings("deprecation")
public class CoconutClusterBlock extends HorizontalDirectionalBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    private final PalmTrees tree;

    public CoconutClusterBlock(Properties properties, PalmTrees tree) {
        super(properties);
        this.tree = tree;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AGE, 0)
                .setValue(ATTACHED, false)
                .setValue(NATURAL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE, ATTACHED, NATURAL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            if (isValidAttachment(context.getLevel(), context.getClickedPos(), facing)) {
                return state.setValue(FACING, facing)
                        .setValue(ATTACHED, true)
                        .setValue(NATURAL, false);
            }
        }
        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidAttachment(level, pos, state.getValue(FACING));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction == state.getValue(FACING)) {
            return state.setValue(ATTACHED, isValidAttachment(level, pos, direction));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(AGE);

        int count = switch (age) {
            case 5 -> 3;
            case 6 -> 2;
            default -> 0;
        };

        if (count != 0) {
            if (!level.isClientSide) {
                level.removeBlock(pos, false);
                popResource(level, pos, new ItemStack(tree.getDroppedFruitBlock().get(), count));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.use(state, level, pos, player, hand, hit);

    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ATTACHED)) {
            if (random.nextFloat() < 0.25f) {
                int age = state.getValue(AGE);
                if (age < 7) {
                    level.setBlock(pos, state.setValue(AGE, age + 1), 2);
                    if (age >= 4) {
                        spawnFallingCoconut(level, pos);
                    }
                } else {
                    spawnFallingCoconut(level, pos);
                }
            }
        }
    }

    private void spawnFallingCoconut(ServerLevel level, BlockPos pos) {
        List<BlockPos> validPositions = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos target = pos.offset(x, -1, z);
                if (level.isEmptyBlock(target)) {
                    validPositions.add(target);
                }
            }
        }

        if (!validPositions.isEmpty()) {
            BlockPos spawnPos = validPositions.get(level.getRandom().nextInt(validPositions.size()));
            FallingBlockEntity entity = FallingBlockEntity.fall(level, spawnPos, tree.getDroppedFruitBlock().get().defaultBlockState());
            entity.setHurtsEntities(2.0f, 2);
            level.removeBlock(pos, false);
        } else {
            popResource(level, pos, new ItemStack(tree.getDroppedFruitBlock().get()));
            level.removeBlock(pos, false);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(ATTACHED);
    }

    private boolean isValidAttachment(LevelReader level, BlockPos pos, Direction facing) {
        BlockState neighbor = level.getBlockState(pos.relative(facing));
        if (neighbor.getBlock() instanceof PalmHeadBlock) {
            return neighbor.hasProperty(PalmHeadBlock.NATURAL) && neighbor.getValue(PalmHeadBlock.NATURAL);
        }
        if (neighbor.getBlock() instanceof CoconutClusterBlock) {
            return neighbor.hasProperty(NATURAL) && neighbor.getValue(NATURAL);
        }
        return false;
    }
}
