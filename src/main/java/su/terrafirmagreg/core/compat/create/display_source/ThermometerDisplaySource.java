package su.terrafirmagreg.core.compat.create.display_source;

import java.util.Objects;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

        if (!state.is(TFCBlocks.THERMOMETER.get())) {
            return ZERO.copy();
        }

        int mode = context.sourceConfig().getInt("Mode");

        // Mode 0: Unit Temperature
        if (mode == 0) {
            return Objects.requireNonNull(unit.format(getTemperature(state, level, pos, isFacing)));
        }
        // Mode 1: Color Temperature
        else if (mode == 1) {
            return Objects.requireNonNull(style.formatColored(getTemperature(state, level, pos, isFacing)));
        }
        // Mode 2: Redstone Power Output
        else {
            int power = state.getOptionalValue(BlockStateProperties.POWER).orElse(0);
            return Component.literal(String.valueOf(power));
        }
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
                    .forOptions(CreateLang.translatedOptions("display_source.thermometer", "unit_temperature", "color_temperature", "redstone_power"))
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
