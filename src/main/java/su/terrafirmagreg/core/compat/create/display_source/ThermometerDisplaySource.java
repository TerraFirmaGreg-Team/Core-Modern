package su.terrafirmagreg.core.compat.create.display_source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("NoTranslation")
public class ThermometerDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        Level level = context.level();
        BlockPos pos = context.getSourcePos();
        BlockState state = level.getBlockState(pos);
        var style = TFCConfig.CLIENT.heatTooltipStyle.get();
        var unit = TFCConfig.CLIENT.climateTooltipStyle.get();
        boolean isFacing = state.hasProperty(TFCBlockStateProperties.THERMOMETER_ATTACHED) && state.getValue(TFCBlockStateProperties.THERMOMETER_ATTACHED);
        float avgTemp = Climate.getAverageTemperature(level, pos);
        float rainfall = Climate.getRainfall(level, pos);
        boolean warm = Climate.warmEnoughToRain(level, pos);

        if (!state.is(TFCBlocks.THERMOMETER.get())) {
            return ZERO.copy();
        }

        int mode = context.sourceConfig().getInt("Mode");

        return switch (mode) {
            case 0 ->
                // Unit Temperature
                unit.format(getTemperature(state, level, pos, isFacing));
            case 1 ->
                // Color Temperature
                style.formatColored(getTemperature(state, level, pos, isFacing));
            case 2 ->
                // Avg Temperature
                unit.format(avgTemp);
            case 3 ->
                // Annual Rainfall
                Component.literal(String.format("%.0fmm", rainfall));
            case 4 -> {
                // Active Precipitation
                if (EnvironmentHelpers.isRainingOrSnowing(level, pos)) {
                    boolean isThundering = level.isThundering();
                    long remainingTicks = 0;
                    if (level.getLevelData() instanceof ServerLevelData serverLevelData) {
                        remainingTicks = isThundering ? serverLevelData.getThunderTime() : serverLevelData.getRainTime();
                    }

                    int daysInMonth = Calendars.get(level).getCalendarDaysInMonth();
                    Component duration = ICalendar.getTimeDelta(remainingTicks, daysInMonth);

                    MutableComponent typeKey;
                    if (isThundering && warm) {
                        typeKey = Component.literal("⛈ ⌛");
                    } else {
                        typeKey = Component.literal(warm ? "\uD83C\uDF27 ⌛" : "❄ ⌛");
                    }

                    yield typeKey.append(duration);
                }

                // Rain chance based on rainfall.
                int dailyRainChance = Math.round(Mth.clamp(rainfall / 5.0f, 0.0f, 100.0f));

                // Rain chance based on storm cycle.
                if (level.getLevelData() instanceof ServerLevelData serverLevelData) {
                    long ticksUntilRain = getTicksUntilRain(serverLevelData);

                    long ticksLeftToday = ICalendar.TICKS_IN_DAY - (Calendars.get(level).getCalendarTicks() % ICalendar.TICKS_IN_DAY);

                    // Probability that a passing storm has enough intensity to rain here.
                    float stormCatchProbability = Mth.clamp(rainfall / 500.0f, 0.0f, 1.0f);

                    if (ticksUntilRain <= ticksLeftToday) {
                        // Storm is arriving on this calendar day.
                        dailyRainChance = Math.round(stormCatchProbability * 90.0f);
                    } else if (ticksUntilRain <= ICalendar.TICKS_IN_DAY) {
                        // Storm arriving within the next 24 hours.
                        float proximity = 1.0f - ((float) (ticksUntilRain - ticksLeftToday) / ICalendar.TICKS_IN_DAY);
                        dailyRainChance = Math.round(stormCatchProbability * proximity * 60.0f);
                    } else {
                        // Clear skies for today.
                        dailyRainChance = Math.round((rainfall / 500.0f) * 10.0f);
                    }
                }

                // Change symbol based on precipitation chance.
                String symbol = "☀ ";
                if (dailyRainChance > 15 && dailyRainChance < 25) {
                    symbol = "☁ ";
                } else if (dailyRainChance >= 30) {
                    symbol = warm ? "\uD83C\uDF27 " : "❄ ";
                }

                yield Component.literal(symbol).append(Component.translatable("tfg.tooltip.climate_precipitation.chance", dailyRainChance));
            }
            case 5 ->
                // Koppen
                Helpers.translateEnum(KoppenClimateClassification.classify(avgTemp, rainfall));
            default -> {
                // Redstone Power Output
                int power = state.getOptionalValue(BlockStateProperties.POWER).orElse(0);
                yield Component.literal(String.valueOf(power));
            }
        };
    }

    /**
     * Calculates ticks until the next storm arrives.
     */
    private static long getTicksUntilRain(ServerLevelData serverLevelData) {
        boolean isGlobalRain = serverLevelData.isRaining();
        int clearWeatherTime = serverLevelData.getClearWeatherTime();
        long rainTime = serverLevelData.getRainTime();

        long ticksUntilRain;
        if (clearWeatherTime > 0) {
            ticksUntilRain = clearWeatherTime + rainTime;
        } else if (isGlobalRain) {
            ticksUntilRain = rainTime + 24000L;
        } else {
            ticksUntilRain = rainTime;
        }
        return ticksUntilRain;
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected String getTranslationKey() {
        return "thermometer";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (!isFirstLine) {
            builder.addSelectionScrollInput(0, 120, (si, l) -> si
                    .forOptions(CreateLang.translatedOptions("display_source.thermometer",
                            "unit_temperature",
                            "color_temperature",
                            "avg_temperature",
                            "annual_rainfall",
                            "precipitation",
                            "koppen",
                            "redstone_power"))
                    .titled(Component.translatable("tfg.display_source.thermometer.display_mode")), "Mode");
        }
    }

    /**
     * Get temperature based on if it's in facing mode or ambient mode.
     */
    private static float getTemperature(BlockState state, Level level, BlockPos pos, Boolean isFacing) {
        float temperature = 1;

        if (isFacing) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockEntity targetBe = level.getBlockEntity(pos.relative(facing.getOpposite()));
            if (targetBe instanceof IHeatable heatable) {
                temperature = heatable.getTemperature();
            }
        } else {
            temperature = Climate.getTemperature(level, pos);
        }
        return temperature;
    }
}
