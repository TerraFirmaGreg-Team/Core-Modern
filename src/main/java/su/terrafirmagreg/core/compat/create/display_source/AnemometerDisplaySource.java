package su.terrafirmagreg.core.compat.create.display_source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings({ "NoTranslation", "deprecation" })
public class AnemometerDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        Level level = context.level();
        BlockPos pos = context.getSourcePos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Vec2 windVector = Climate.getWindVector(level, pos);
        String[] cardinals = { "n", "ne", "e", "se", "s", "sw", "w", "nw" };

        float windSpeedMS = windVector.length() * 20.0F;
        float windAngleRad = (float) Mth.atan2(windVector.y, windVector.x);
        float windAngleDeg = (windAngleRad * Mth.RAD_TO_DEG + 90.0F) % 360.0F;
        int cardinalIndex = Mth.positiveModulo(Mth.floor(windAngleDeg / 45.0F), 8);
        String cardinalDirection = cardinals[cardinalIndex];

        if (!state.is(TFCBlocks.ANEMOMETER.get()) && !state.is(TFCBlocks.VANE.get())) {
            return ZERO.copy();
        }

        int mode = context.sourceConfig().getInt("Mode");
        return switch (mode) {
            case 0 ->
                // Wind Angle
                Component.translatable("tfg.tooltip.climate_wind.angl_" + cardinalDirection, String.format("%.1f", windAngleDeg));
            case 1 ->
                // Wind Speed m/s
                Component.translatable("tfg.tooltip.climate_wind.speed_ms", String.format("%.1f", windSpeedMS));
            case 2 ->
                // Wind Speed blocks/s
                Component.translatable("tfg.tooltip.climate_wind.speed_bs", String.format("%.1f", windSpeedMS));
            case 3 ->
                // Wind Speed Knots
                Component.translatable("tfg.tooltip.climate_wind.speed_knots", String.format("%.1f", windSpeedMS * 1.94F));
            default -> {
                int power = block.getSignal(state, level, pos, Direction.NORTH);
                yield Component.literal(String.valueOf(power));
            }
        };
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected String getTranslationKey() {
        return "anemometer";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (!isFirstLine) {
            builder.addSelectionScrollInput(0, 120, (si, l) -> si
                    .forOptions(CreateLang.translatedOptions("display_source.anemometer",
                            "wind_angle",
                            "wind_speed_ms",
                            "wind_speed_bs",
                            "wind_speed_knots",
                            "redstone_power"))
                    .titled(Component.translatable("tfg.display_source.anemometer.display_mode")), "Mode");
        }
    }
}
