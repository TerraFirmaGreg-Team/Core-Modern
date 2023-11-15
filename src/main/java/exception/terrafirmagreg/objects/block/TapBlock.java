package exception.terrafirmagreg.objects.block;

import exception.terrafirmagreg.TerraFirmaGreg;
import exception.terrafirmagreg.objects.entity.TapBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TapBlock extends BaseEntityBlock {
    private static final Map<Direction, VoxelShape> SHAPES = new HashMap<>() {{
        put(Direction.NORTH, box(6D, 3D, 9D, 10D, 10D, 16D));
        put(Direction.SOUTH, box(6D, 3D, 0D, 10D, 10D, 7D));
        put(Direction.WEST, box(9D, 3D, 6D, 16D, 10D, 10D));
        put(Direction.EAST, box(0D, 3D, 6D, 7D, 10D, 10D));
    }};
    public static Map<BlockPos, float[]> color = new HashMap<>();
    private final float modifier;

    public TapBlock(BlockBehaviour.Properties properties, float modifier) {
        super(properties);
        this.modifier = modifier;

        this.registerDefaultState(this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(BlockStateProperties.ATTACHED, false));
    }

    private static float[] getParticleColor(BlockPos pos) {
        return color.get(pos);
    }

    private static boolean hasParticleColor(BlockPos pos) {
        return color.containsKey(pos);
    }

    public float getModifier() {
        return modifier;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TapBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, TerraFirmaGreg.TAP_BLOCK_ENTITY.get(), TapBlockEntity::tick);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(HorizontalDirectionalBlock.FACING));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(HorizontalDirectionalBlock.FACING).add(BlockStateProperties.ATTACHED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace().getAxis().isHorizontal()) {
            return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getClickedFace());
        }
        return this.defaultBlockState();
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
        return level.getBlockState(pos.relative(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite())).is(TerraFirmaGreg.TAPPABLE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockState(pos.below()).isAir()) {
            BlockState collectorState = player.getItemInHand(hand).is(TerraFirmaGreg.METAL_BUCKETS) ? TerraFirmaGreg.SAP_COLLECTOR.get()
                    .defaultBlockState() : (
                    player.getItemInHand(hand).is(TerraFirmaGreg.WOODEN_BUCKETS) ? TerraFirmaGreg.WOODEN_SAP_COLLECTOR.get()
                            .defaultBlockState() : null);
            if (collectorState != null) {
                level.setBlockAndUpdate(pos.below(), collectorState.setValue(HorizontalDirectionalBlock.FACING, state.getValue(HorizontalDirectionalBlock.FACING)));
                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }
                return InteractionResult.CONSUME;
            }
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        // If the block below was changed, reset recipe
        if (pos.getY() - fromPos.getY() == 1 && level.getBlockEntity(pos) instanceof TapBlockEntity tapBlockEntity) {
            tapBlockEntity.reset();
            level.sendBlockUpdated(pos, state, state.setValue(BlockStateProperties.ATTACHED, false), Block.UPDATE_CLIENTS);
        }
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 4);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(BlockStateProperties.ATTACHED)) {
            for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                this.trySpawnDripParticles(level, pos, state);
            }
        }
    }

    private void trySpawnDripParticles(Level level, BlockPos pos, BlockState state) {
        if (!(level.random.nextFloat() < 0.3F)) {
            this.spawnParticle(level, pos, state);
        }
    }

    private void spawnParticle(Level level, BlockPos pos, BlockState state) {
        var particle = TerraFirmaGreg.COLORED_DRIP_PARTICLE.get();

        if (hasParticleColor(pos)) {
            particle.setColor(getParticleColor(pos));
        } else {
            particle.setColor(new float[]{0.92F, 0.782F, 0.72F});
        }

        float xd = switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
            case EAST -> 0.41f;
            case WEST -> 0.59f;
            default -> 0.5f;
        };
        float zd = switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
            case NORTH -> 0.59f;
            case SOUTH -> 0.41f;
            default -> 0.5f;
        };

        level.addParticle(particle, (double) pos.getX() + xd, (double) pos.getY() + 0.1875D, (double) pos.getZ() + zd, 0.0D, 0.0D, 0.0D);
    }
}
