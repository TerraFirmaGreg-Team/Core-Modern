package su.terrafirmagreg.core.common.block.palmtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.PalmTrees;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

@SuppressWarnings("deprecation")
public class GrowingPalmHeadBlock extends Block implements EntityBlockExtension, HoeOverlayBlock {

    public static final IntegerProperty STAGE = TFCBlockStateProperties.STAGE_3;
    public static final BooleanProperty NATURAL = TFCBlockStateProperties.NATURAL;

    public static final VoxelShape SHAPE0 = box(5, 0, 5, 11, 16, 11);
    public static final VoxelShape SHAPE1 = box(2, 0, 2, 14, 16, 14);
    public static final VoxelShape SHAPE2 = box(1, 0, 1, 15, 16, 15);

    private static final Map<Integer, LeafPattern> LEAF_PATTERNS = Map.of(
            1, new LeafPattern(
                    Map.of(
                            0, new String[] {
                                    "   ",
                                    " H ",
                                    "   "
                            },
                            1, new String[] {
                                    " L ",
                                    "LLL",
                                    " L "
                            })),
            2, new LeafPattern(
                    Map.of(
                            0, new String[] {
                                    "  L  ",
                                    "     ",
                                    "L H L",
                                    "     ",
                                    "  L  "
                            },
                            1, new String[] {
                                    "  L  ",
                                    "  L  ",
                                    "LLLLL",
                                    "  L  ",
                                    "  L  "
                            },
                            2, new String[] {
                                    "     ",
                                    " LLL ",
                                    " LLL ",
                                    " LLL ",
                                    "     "
                            },
                            3, new String[] {
                                    "  L  ",
                                    "  L  ",
                                    "LL LL",
                                    "  L  ",
                                    "  L  "
                            })),
            3, new LeafPattern(
                    Map.of(
                            0, new String[] {
                                    "   L   ",
                                    "   L   ",
                                    "       ",
                                    "LL H LL",
                                    "       ",
                                    "   L   ",
                                    "   L   "
                            },
                            1, new String[] {
                                    "       ",
                                    "   L   ",
                                    "   L   ",
                                    " LLLLL ",
                                    "   L   ",
                                    "   L   ",
                                    "       "
                            },
                            2, new String[] {
                                    "       ",
                                    "       ",
                                    "  LLL  ",
                                    "  LLL  ",
                                    "  LLL  ",
                                    "       ",
                                    "       "
                            },
                            3, new String[] {
                                    "       ",
                                    "   L   ",
                                    "   L   ",
                                    " LL LL ",
                                    "   L   ",
                                    "   L   ",
                                    "       "
                            },
                            4, new String[] {
                                    "   L   ",
                                    "   L   ",
                                    "       ",
                                    "LL   LL",
                                    "       ",
                                    "   L   ",
                                    "   L   "
                            })));

    private final ExtendedProperties extendedProperties;
    private final PalmTrees tree;
    private final Supplier<ClimateRange> climateRange;
    private final Lifecycle[] stages;

    public GrowingPalmHeadBlock(ExtendedProperties properties, PalmTrees tree, Supplier<ClimateRange> climateRange, Lifecycle[] stages) {
        super(properties.properties());
        this.extendedProperties = properties;
        this.tree = tree;
        this.stages = stages;
        this.climateRange = climateRange;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STAGE, 0)
                .setValue(NATURAL, false));
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return extendedProperties;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, NATURAL);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        final float temp = Climate.getAverageTemperature(level, pos);
        final int hydration = (int) (Climate.getRainfall(level, pos) / 5);

        var calendar = Calendars.get(level);
        Month month = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());

        final boolean natural = state.getValue(NATURAL);
        final boolean climateOk = climateRange.get().checkBoth(hydration, temp, false);
        final boolean dormant = stages[month.ordinal()] == Lifecycle.DORMANT;

        if (!climateOk && !natural) {
            TickCounterBlockEntity.reset(level, pos);
        }

        if (!dormant && (natural || climateOk)) {
            if (level.getBlockEntity(pos) instanceof TickCounterBlockEntity counter) {
                long days = counter.getTicksSinceUpdate() / ICalendar.TICKS_IN_DAY;
                if (days >= 5) {
                    grow(level, pos, state, random);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.isFaceSturdy(level, pos.below(), Direction.UP)
                || below.is(TFGBlocks_PalmTrees.PALM_TRUNK.get())
                || below.is(TFGBlocks_PalmTrees.PALM_HEADS.get(tree).get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(STAGE)) {
            case 0 -> SHAPE0;
            case 1 -> SHAPE1;
            default -> SHAPE2;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            if (level instanceof Level l && !l.isClientSide) {
                l.destroyBlock(pos, true);
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    public void grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        int stage = state.getValue(STAGE);
        boolean natural = state.getValue(NATURAL);

        if (stage == 0) {
            // 2 trunks at size 2
            if (canGrowUp(level, pos, 2)) {
                placeTrunk(level, pos, 2);
                placeTrunk(level, pos.above(1), 2);

                BlockPos newHeadPos = pos.above(2);
                level.setBlock(newHeadPos, state.setValue(STAGE, 1), 3);
                placeLeaves(level, newHeadPos, 1);

                TickCounterBlockEntity.reset(level, newHeadPos);
            }
        } else if (stage == 1) {
            if (canGrowUp(level, pos, 3)) {
                // 2 trunks at size 2
                placeTrunk(level, pos.above(2), 2);
                placeTrunk(level, pos.above(1), 2);
                // 3 trunks at size 1
                placeTrunk(level, pos, 1);
                placeTrunk(level, pos.below(1), 1);
                placeTrunk(level, pos.below(2), 1);

                clearLeaves(level, pos, 1);
                BlockPos newHeadPos = pos.above(3);
                level.setBlock(newHeadPos, state.setValue(STAGE, 2), 3);
                placeLeaves(level, newHeadPos, 2);

                TickCounterBlockEntity.reset(level, newHeadPos);
            }
        } else if (stage == 2) {
            int numSize2 = random.nextIntBetweenInclusive(tree.getMinGrowthSize(), tree.getMaxGrowthSize());
            if (canGrowUp(level, pos, numSize2)) {
                // Random trunks between min and max tree size at size 2
                for (int i = 0; i < numSize2; i++) {
                    placeTrunk(level, pos.above(i), 2);
                }
                // 3 trunks at size 1
                placeTrunk(level, pos.below(1), 1);
                placeTrunk(level, pos.below(2), 1);
                placeTrunk(level, pos.below(3), 1);
                // 2 trunks at size 0
                placeTrunk(level, pos.below(4), 0);
                placeTrunk(level, pos.below(5), 0);

                clearLeaves(level, pos, 2);
                BlockPos finalHeadPos = pos.above(numSize2);
                BlockState finalHead = TFGBlocks_PalmTrees.PALM_HEADS.get(tree).get().defaultBlockState().setValue(PalmHeadBlock.NATURAL, natural);
                level.setBlock(finalHeadPos, finalHead, 3);
                placeLeaves(level, finalHeadPos, 3);
            }
        }
    }

    private void placeLeaves(Level level, BlockPos headPos, int stage) {
        if (LEAF_PATTERNS.containsKey(stage)) {
            LEAF_PATTERNS.get(stage).place(level, headPos);
        }
    }

    private void clearLeaves(Level level, BlockPos headPos, int stage) {
        if (LEAF_PATTERNS.containsKey(stage)) {
            LEAF_PATTERNS.get(stage).clear(level, headPos);
        }
    }

    private static class LeafPattern {
        private final List<BlockPos> leafPositions;

        public LeafPattern(Map<Integer, String[]> layers) {
            this.leafPositions = calculatePositions(layers);
        }

        private static List<BlockPos> calculatePositions(Map<Integer, String[]> layers) {
            Map<BlockPos, Character> pattern = new HashMap<>();
            BlockPos headPosInPattern = null;

            for (Map.Entry<Integer, String[]> entry : layers.entrySet()) {
                int y = entry.getKey();
                String[] rows = entry.getValue();
                for (int z = 0; z < rows.length; z++) {
                    for (int x = 0; x < rows[z].length(); x++) {
                        char c = rows[z].charAt(x);
                        BlockPos pos = new BlockPos(x, y, z);
                        pattern.put(pos, c);
                        if (c == 'H') {
                            headPosInPattern = pos;
                        }
                    }
                }
            }

            if (headPosInPattern == null) {
                return List.of();
            }

            List<BlockPos> result = new ArrayList<>();
            for (Map.Entry<BlockPos, Character> entry : pattern.entrySet()) {
                if (entry.getValue() == 'L') {
                    result.add(entry.getKey().subtract(headPosInPattern));
                }
            }
            return result;
        }

        public void place(Level level, BlockPos headPos) {
            BlockState leaves = TFGBlocks_PalmTrees.FRUIT_PALM_LEAVES.get().defaultBlockState()
                    .setValue(BlockStateProperties.PERSISTENT, false);

            for (BlockPos relativePos : leafPositions) {
                BlockPos leafPos = headPos.offset(relativePos);
                BlockState currentState = level.getBlockState(leafPos);
                if (currentState.isAir() || currentState.is(BlockTags.LEAVES) || Helpers.isBlock(currentState, TFCTags.Blocks.FRUIT_TREE_LEAVES)) {
                    level.setBlock(leafPos, leaves, 3);
                }
            }
        }

        public void clear(Level level, BlockPos headPos) {
            for (BlockPos relativePos : leafPositions) {
                BlockPos leafPos = headPos.offset(relativePos);
                BlockState state = level.getBlockState(leafPos);
                if (state.is(BlockTags.LEAVES) || Helpers.isBlock(state, TFCTags.Blocks.FRUIT_TREE_LEAVES)) {
                    level.setBlock(leafPos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static boolean canGrowInto(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.LEAVES) || Helpers.isBlock(state, TFCTags.Blocks.FRUIT_TREE_LEAVES);
    }

    private boolean canGrowUp(Level level, BlockPos pos, int height) {
        for (int i = 1; i <= height; i++) {
            if (!canGrowInto(level, pos.above(i))) {
                return false;
            }
        }
        return pos.getY() + height < level.getMaxBuildHeight();
    }

    private void placeTrunk(Level level, BlockPos pos, int size) {
        Block trunkBlock = TFGBlocks_PalmTrees.PALM_TRUNK.get();
        BlockState state = trunkBlock.defaultBlockState().setValue(PalmTrunkBlock.SIZE, size);
        IFluidLoggable fluidLoggable = (IFluidLoggable) trunkBlock;
        state = state.setValue(fluidLoggable.getFluidProperty(), fluidLoggable.getFluidProperty().keyForOrEmpty(level.getFluidState(pos).getType()));
        level.setBlock(pos, state, 3);
    }

    @Override
    public void addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug) {
        if (!state.getValue(NATURAL))
            return;
        final ClimateRange range = climateRange.get();
        final int hydration = (int) (Climate.getRainfall(level, pos) / 5);

        text.add(FarmlandBlock.getHydrationTooltip(level, pos, range, false, hydration));
        text.add(FarmlandBlock.getAverageTemperatureTooltip(level, pos, range, false));

        var calendar = Calendars.get(level);
        Month month = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
        if (stages[month.ordinal()] != Lifecycle.DORMANT) {
            text.add(Component.translatable("tfg.tooltip.lifecycle.growing"));
        } else {
            text.add(Component.translatable("tfc.tooltip.fruit_tree.growing"));
        }
    }
}
