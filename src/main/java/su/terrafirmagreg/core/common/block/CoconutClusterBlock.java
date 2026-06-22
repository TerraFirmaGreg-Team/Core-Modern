package su.terrafirmagreg.core.common.block;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.plant.fruit.IBushBlock;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.blockentity.PalmClusterBlockEntity;
import su.terrafirmagreg.core.common.data.PalmTrees;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

@SuppressWarnings("deprecation")
public class CoconutClusterBlock extends HorizontalDirectionalBlock implements EntityBlock, IBushBlock, HoeOverlayBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    private static final VoxelShape[] SHAPES = Helpers.computeHorizontalShapes(dir -> Helpers.rotateShape(dir, 1, 1, 0, 15, 15, 12));

    private record CoconutDrop(int count, Block type, boolean spawnFallingCoconut, boolean finalAge) {
    }

    private final PalmTrees tree;
    private final Block brownCoconut;
    private final Block greenCoconut;

    public CoconutClusterBlock(Properties properties, PalmTrees tree) {
        super(properties);
        this.tree = tree;
        this.brownCoconut = tree.getDroppedFruitBlock().get();
        this.greenCoconut = TFGBlocks_PalmTrees.GREEN_COCONUT.get();
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AGE, 0)
                .setValue(ATTACHED, false)
                .setValue(NATURAL, false));
    }

    private CoconutDrop getDropForAge(int age) {
        Block type;
        boolean spawnFallingCoconut = false;
        boolean finalAge = false;
        int count = switch (age) {
            case 1, 2, 3 -> {
                type = greenCoconut;
                yield 3;
            }
            case 4 -> {
                type = brownCoconut;
                yield 3;
            }
            case 5 -> {
                type = brownCoconut;
                spawnFallingCoconut = true;
                yield 2;
            }
            case 6 -> {
                type = brownCoconut;
                spawnFallingCoconut = true;
                yield 1;
            }
            case 7 -> {
                type = brownCoconut;
                spawnFallingCoconut = true;
                finalAge = true;
                yield 0;
            }
            default -> {
                type = Blocks.AIR;
                yield 0;
            }
        };
        return new CoconutDrop(count, type, spawnFallingCoconut, finalAge);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get2DDataValue()];
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
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {

        BlockPos pos = hit.getBlockPos();

        if (!level.isClientSide() && !(projectile instanceof Snowball)) {
            CoconutDrop drop = getDropForAge(state.getValue(AGE));

            for (int i = 0; i < drop.count; i++) {
                spawnFallingCoconut((ServerLevel) level, pos, drop.type);
            }

            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BAMBOO_HIT, SoundSource.AMBIENT, 2.0f, 0.1f);

            if (level instanceof ServerLevel serverLevel) {
                if (drop.count == 0) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX(), pos.getY(), pos.getZ(), 5, 0.1, 0.1, 0.1, 0.5);
                }
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, drop.type.defaultBlockState()), pos.getX(), pos.getY(), pos.getZ(), 10, 0.1, 0.1, 0.1, 0.5);
            }
        }
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
        CoconutDrop drop = getDropForAge(state.getValue(AGE));
        if (!player.getItemInHand(player.getUsedItemHand()).isEmpty())
            return InteractionResult.FAIL;

        if (drop.count != 0) {
            if (!level.isClientSide) {
                level.removeBlock(pos, false);
                popResource(level, pos, new ItemStack(drop.type, drop.count));

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BAMBOO_HIT, SoundSource.AMBIENT, 0.5f, 2.0f);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, drop.type.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1,
                            0.5);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.use(state, level, pos, player, hand, hit);

    }

    @Override
    public void addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug) {
        final ClimateRange range = tree.getClimateRange().get();
        final int hydration = (int) (Climate.getRainfall(level, pos) / 5);
        text.add(FarmlandBlock.getHydrationTooltip(level, pos, range, false, hydration));
        text.add(FarmlandBlock.getAverageTemperatureTooltip(level, pos, range, false));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        IBushBlock.randomTick(this, state, level, pos, random);
    }

    @Override
    public void onUpdate(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(ATTACHED)) {
            return;
        }

        ClimateRange range = tree.getClimateRange().get();
        int hydration = (int) (Climate.getRainfall(level, pos) / 5);
        float temperature = Climate.getAverageTemperature(level, pos);

        if (range.checkBoth(hydration, temperature, false)) {
            int age = state.getValue(AGE);
            CoconutDrop drop = getDropForAge(age);

            if (drop.finalAge) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                if (level instanceof ServerLevel serverLevel) {
                    spawnFallingCoconut(serverLevel, pos, drop.type);
                }
            } else {
                age++;
                state = state.setValue(AGE, age);
                level.setBlock(pos, state, 2);

                CoconutDrop newDrop = getDropForAge(age);
                if (newDrop.spawnFallingCoconut && !newDrop.finalAge) {
                    if (level instanceof ServerLevel serverLevel) {
                        spawnFallingCoconut(serverLevel, pos, newDrop.type);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, p, s, be) -> {
            if (be instanceof PalmClusterBlockEntity cluster) {
                PalmClusterBlockEntity.serverTick(l, p, s, cluster);
            }
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TFGBlockEntities.PALM_CLUSTERS.get().create(pos, state);
    }

    private void spawnFallingCoconut(ServerLevel level, BlockPos pos, Block block) {
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
            FallingBlockEntity entity = FallingBlockEntity.fall(level, spawnPos, block.defaultBlockState());
            entity.setHurtsEntities(2.0f, 2);
        } else {
            popResource(level, pos, new ItemStack(block));
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
