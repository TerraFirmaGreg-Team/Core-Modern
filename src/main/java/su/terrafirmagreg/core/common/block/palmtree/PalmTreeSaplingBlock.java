package su.terrafirmagreg.core.common.block.palmtree;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.common.blocks.*;
import net.dries007.tfc.common.blocks.plant.Plant;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.SeasonalPlantBlock;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PalmTreeSaplingBlock extends BushBlock implements IForgeBlockExtension, EntityBlockExtension, HoeOverlayBlock {

    private final ExtendedProperties properties;
    private final Supplier<ClimateRange> climateRange;
    protected final Supplier<? extends Block> block;
    protected final Supplier<Integer> treeGrowthDays;
    private final Lifecycle[] stages;

    public PalmTreeSaplingBlock(ExtendedProperties properties, Supplier<? extends Block> block, int treeGrowthDays, Supplier<ClimateRange> climateRange, Lifecycle[] stages) {
        this(properties, block, () -> treeGrowthDays, climateRange, stages);
    }

    public PalmTreeSaplingBlock(ExtendedProperties properties, Supplier<? extends Block> block, Supplier<Integer> treeGrowthDays, Supplier<ClimateRange> climateRange, Lifecycle[] stages) {
        super(properties.properties());
        this.properties = properties;
        this.climateRange = climateRange;
        this.block = block;
        this.treeGrowthDays = treeGrowthDays;
        this.stages = stages;
    }

    @Override
    public void addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug) {
        final ClimateRange range = climateRange.get();
        final int hydration = (int) (Climate.getRainfall(level, pos) / 5);

        text.add(FarmlandBlock.getHydrationTooltip(level, pos, range, false, hydration));
        text.add(FarmlandBlock.getAverageTemperatureTooltip(level, pos, range, false));

        var calendar = Calendars.get(level);
        Month month = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
        if (!stages[month.ordinal()].active()) {
            text.add(Component.translatable("tfc.tooltip.fruit_tree.sapling_wrong_month"));
        } else {
            text.add(Component.translatable("tfc.tooltip.fruit_tree.growing"));
        }

        /*
         In TFC the growth time tooltip is handled by `BlockEntityTooltips` but it's hardcoded to work for specific block classes,
         and I didn't feel like making a mixin, so I just copied it.
        */
        if (level.getBlockEntity(pos) instanceof TickCounterBlockEntity counter) {
            long ticks = (long) ((double) (this.getTreeGrowthDays() * 24000) * TFCConfig.SERVER.globalFruitSaplingGrowthModifier.get()) - counter.getTicksSinceUpdate();
            if (ticks > 0L) {
                text.add(Component.translatable("tfc.jade.time_left", Calendars.get(level).getTimeDelta(ticks)));
            } else {
                text.add(Component.translatable("tfc.jade.ready_to_grow"));
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SeasonalPlantBlock.PLANT_SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        var calendar = Calendars.get(level);
        Month month = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
        if (stages[month.ordinal()].active()) {
            if (level.getBlockEntity(pos) instanceof TickCounterBlockEntity counter) {
                if (counter.getTicksSinceUpdate() > ICalendar.TICKS_IN_DAY * getTreeGrowthDays() * TFCConfig.SERVER.globalFruitSaplingGrowthModifier.get()) {
                    final int hydration = (int) (Climate.getRainfall(level, pos) / 5);
                    final float temp = Climate.getAverageTemperature(level, pos);
                    if (!climateRange.get().checkBoth(hydration, temp, false)) {
                        level.setBlockAndUpdate(pos, TFCBlocks.PLANTS.get(Plant.DEAD_BUSH).get().defaultBlockState());
                    } else {
                        createTree(level, pos, state, random);
                    }
                }
            }
        }
    }

    public void createTree(Level level, BlockPos pos, BlockState state, RandomSource random) {
        BlockState grownState = block.get().defaultBlockState();
        if (grownState.hasProperty(PalmHeadBlock.NATURAL)) {
            grownState = grownState.setValue(PalmHeadBlock.NATURAL, true);
        }
        level.setBlockAndUpdate(pos, grownState);
        TickCounterBlockEntity.reset(level, pos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos downPos = pos.below();
        BlockState downState = level.getBlockState(downPos);
        return super.canSurvive(state, level, pos) || Helpers.isBlock(downState, TFCTags.Blocks.BUSH_PLANTABLE_ON);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        TickCounterBlockEntity.reset(level, pos);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    public int getTreeGrowthDays() {
        return treeGrowthDays.get();
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return properties;
    }
}
