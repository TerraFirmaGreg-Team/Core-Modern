package exception.terrafirmagreg.objects.block;

import exception.terrafirmagreg.TerraFirmaGreg;
import exception.terrafirmagreg.objects.entity.SapCollectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class SapCollectorBlock extends BaseEntityBlock {
    private static final Map<Direction, VoxelShape> SHAPES = new HashMap<>() {{
        put(Direction.NORTH, Shapes.join(box(2.5D, 4D, 5D, 13.5D, 14D, 16D), box(3.5D, 5D, 6D, 12.5D, 14D, 15D), BooleanOp.ONLY_FIRST));
        put(Direction.SOUTH, Shapes.join(box(2.5D, 4D, 0D, 13.5D, 14D, 11D), box(3.5D, 5D, 1D, 12.5D, 14D, 10D), BooleanOp.ONLY_FIRST));
        put(Direction.WEST, Shapes.join(box(5D, 4D, 2.5D, 16D, 14D, 13.5D), box(6D, 5D, 3.5D, 15D, 14D, 12.5D), BooleanOp.ONLY_FIRST));
        put(Direction.EAST, Shapes.join(box(0D, 4D, 2.5D, 11D, 14D, 13.5D), box(1D, 5D, 3.5D, 10D, 14D, 12.5D), BooleanOp.ONLY_FIRST));
    }};

    public SapCollectorBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SapCollectorBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(HorizontalDirectionalBlock.FACING));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(HorizontalDirectionalBlock.FACING);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getClickedFace().getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.relative(Direction.UP)).is(TerraFirmaGreg.TAP.get());
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
        if (!pLevel.isClientSide) {
            pLevel.scheduleTick(pPos, this, 4);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        var tag = state.is(TerraFirmaGreg.WOODEN_SAP_COLLECTOR.get()) ? TerraFirmaGreg.WOODEN_BUCKETS : TerraFirmaGreg.METAL_BUCKETS;
        var bucketItem = BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator();
        return bucketItem.hasNext() ? new ItemStack(bucketItem.next()) : ItemStack.EMPTY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SapCollectorBlockEntity sapCollectorBlock) {
                if (sapCollectorBlock.currentRecipe != null && sapCollectorBlock.progress >= sapCollectorBlock.currentRecipe.processingTime) {
                    ItemStack heldItem = player.getItemInHand(hand);
                    boolean hasCorrectItem = sapCollectorBlock.currentRecipe.harvestItem.isEmpty() && heldItem.isEmpty() || sapCollectorBlock.currentRecipe.harvestItem.test(heldItem);
                    if (hasCorrectItem) {
                        if (sapCollectorBlock.currentRecipe.collectBucket) {
                            player.setItemInHand(hand, sapCollectorBlock.currentRecipe.getResultItem(state));
                            level.playSound(player, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS);
                        } else {
                            popResource(level, pos, sapCollectorBlock.currentRecipe.getResultItem(state));
                            level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS);
                        }
                        player.swing(hand);
                        if (sapCollectorBlock.currentRecipe.collectBucket) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        }
                        if (!sapCollectorBlock.currentRecipe.harvestItem.isEmpty() && !player.isCreative()) {
                            heldItem.shrink(1);
                        }
                        sapCollectorBlock.progress = 0;
                        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }
}
