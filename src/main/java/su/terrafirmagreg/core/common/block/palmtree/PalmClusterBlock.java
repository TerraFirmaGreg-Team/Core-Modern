package su.terrafirmagreg.core.common.block.palmtree;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.plant.fruit.IBushBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;

import su.terrafirmagreg.core.common.blockentity.PalmClusterBlockEntity;
import su.terrafirmagreg.core.common.data.PalmTrees;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

/**
 * Represents a default palm cluster block.
 */
@SuppressWarnings("deprecation")
public class PalmClusterBlock extends HorizontalDirectionalBlock implements EntityBlock, IBushBlock, HoeOverlayBlock {

    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    private static final ThreadLocal<@Nullable IntegerProperty> PROPERTY_TO_REGISTER = new ThreadLocal<>();

    private static final VoxelShape[] SHAPES = Helpers.computeHorizontalShapes(dir -> Helpers.rotateShape(dir, 1, 1, 0, 15, 15, 12));

    protected final PalmTrees tree;
    public final IntegerProperty clusterAge;

    /**
     * Constructs a new cluster block.
     * @param properties Block properties.
     * @param tree Palm tree type.
     */
    public PalmClusterBlock(Properties properties, PalmTrees tree) {
        this(properties, tree, tree.getClusterAgeProperty());
    }

    protected PalmClusterBlock(Properties properties, PalmTrees tree, IntegerProperty clusterAge) {
        super(setup(properties, clusterAge));
        this.tree = tree;
        this.clusterAge = clusterAge;
        PROPERTY_TO_REGISTER.remove();
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(this.clusterAge, 0)
                .setValue(ATTACHED, false)
                .setValue(NATURAL, false));
    }

    private static Properties setup(Properties properties, IntegerProperty property) {
        PROPERTY_TO_REGISTER.set(property);
        return properties;
    }

    /**
     * Gets all drops per each age of the cluster.
     * @param level The level.
     * @param pos The position.
     * @return A map of age to list of drops.
     */
    public Map<Integer, List<ItemStack>> getAllDropsPerAge(ServerLevel level, BlockPos pos) {
        Map<Integer, List<ItemStack>> dropsPerAgeMap = new HashMap<>();

        for (int age : this.clusterAge.getPossibleValues()) {
            BlockState simulatedState = this.defaultBlockState().setValue(this.clusterAge, age);
            List<ItemStack> dropsForThisAge = getDrops(simulatedState, level, pos, null, null, ItemStack.EMPTY);
            dropsPerAgeMap.put(age, dropsForThisAge);
        }

        return dropsPerAgeMap;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        IntegerProperty property = PROPERTY_TO_REGISTER.get();
        builder.add(FACING, property, ATTACHED, NATURAL);
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

    /**
     * Allows clusters to break when hit by projectiles except snowballs.
     */
    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {

        BlockPos pos = hit.getBlockPos();

        if (level instanceof ServerLevel serverLevel && !(projectile instanceof Snowball)) {
            level.destroyBlock(pos, false);

            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.SLIME_BLOCK_BREAK, SoundSource.AMBIENT, 0.5f, 2.0f);

            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.5);

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

        if (level instanceof ServerLevel serverLevel) {
            level.removeBlock(pos, false);
            final BlockEntity entity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            getDrops(state, serverLevel, pos, entity, null, ItemStack.EMPTY).forEach(stack -> ItemHandlerHelper.giveItemToPlayer(player, stack));

            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.SLIME_BLOCK_BREAK, SoundSource.AMBIENT, 0.5f, 2.0f);

            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.5);

        }
        return InteractionResult.sidedSuccess(level.isClientSide);

    }

    @Override
    public void addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug) {
        if (!state.getValue(NATURAL))
            return;

        final ClimateRange range = tree.getClimateRange().get();
        final int hydration = (int) (Climate.getRainfall(level, pos) / 5);
        text.add(FarmlandBlock.getHydrationTooltip(level, pos, range, false, hydration));
        text.add(FarmlandBlock.getAverageTemperatureTooltip(level, pos, range, false));

        var calendar = Calendars.get(level);
        Month month = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
        Lifecycle lifecycle = tree.getStages()[month.ordinal()];
        text.add(Component.translatable("tfg.tooltip.lifecycle." + lifecycle.getSerializedName()));

        if (level instanceof ServerLevel serverLevel) {
            Map<Integer, List<ItemStack>> allDrops = getAllDropsPerAge(serverLevel, pos);
            Map<Item, Integer> mergedDrops = new HashMap<>();

            for (List<ItemStack> ageDrops : allDrops.values()) {
                Map<Item, Integer> ageMerged = new HashMap<>();
                for (ItemStack stack : ageDrops) {
                    if (!stack.isEmpty()) {
                        ageMerged.merge(stack.getItem(), stack.getCount(), Integer::sum);
                    }
                }
                for (Map.Entry<Item, Integer> entry : ageMerged.entrySet()) {
                    mergedDrops.merge(entry.getKey(), entry.getValue(), Math::max);
                }
            }

            for (Map.Entry<Item, Integer> entry : mergedDrops.entrySet()) {
                Item dropItem = entry.getKey();
                int dropCount = entry.getValue();

                if (dropCount > 0) {
                    text.add(Component.translatable("tfg.tooltip.palm_tree.cluster_harvest",
                            Component.empty().append(dropItem.getName(new ItemStack(dropItem))).withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE),
                            Component.literal(String.valueOf(dropCount)).withStyle(ChatFormatting.WHITE)));
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        IBushBlock.randomTick(this, state, level, pos, random);
    }

    /**
     * Handles the growth of the cluster.
     */
    @Override
    public void onUpdate(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(ATTACHED)) {
            return;
        }

        ClimateRange range = tree.getClimateRange().get();
        int hydration = (int) (Climate.getRainfall(level, pos) / 5);
        float temperature = Climate.getAverageTemperature(level, pos);

        var calendar = Calendars.get(level);
        Month month = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
        final boolean dormant = tree.getStages()[month.ordinal()] == Lifecycle.DORMANT;

        if (range.checkBoth(hydration, temperature, false)) {
            int age = state.getValue(this.clusterAge);
            age++;

            if (dormant) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }

            if (age < (tree.getClusterAges() - 1)) {
                level.setBlock(pos, state.setValue(this.clusterAge, age), 2);
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

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(ATTACHED);
    }

    private boolean isValidAttachment(LevelReader level, BlockPos pos, Direction facing) {
        BlockState neighbor = level.getBlockState(pos.relative(facing));
        if (neighbor.getBlock() instanceof PalmHeadBlock) {
            return neighbor.hasProperty(PalmHeadBlock.NATURAL) && neighbor.getValue(PalmHeadBlock.NATURAL);
        }
        if (neighbor.getBlock() instanceof PalmClusterBlock) {
            return neighbor.hasProperty(NATURAL) && neighbor.getValue(NATURAL);
        }
        return false;
    }
}
