package su.terrafirmagreg.core.compat.create.display_source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import earth.terrarium.adastra.common.registry.ModBlocks;

import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

@SuppressWarnings({ "NoTranslation", "deprecation" })
public class EnvironmentApiDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        Level level = context.level();
        BlockPos pos = context.getSourcePos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        MutableComponent oxygen = EnvironmentSystem.hasOxygen(level, pos)
                ? Component.translatable("tfg.tooltip.recipe_condition.oxygenated.true")
                : Component.translatable("tfg.tooltip.recipe_condition.oxygenated.false");
        float pressure = EnvironmentSystem.getPressure(level, pos);
        float gravity = EnvironmentSystem.getGravity(level, pos);

        if (!state.is(ModBlocks.OXYGEN_SENSOR.get())) {
            return ZERO.copy();
        }

        int mode = context.sourceConfig().getInt("Mode");
        return switch (mode) {
            case 0 ->
                // Oxygenated
                oxygen;
            case 1 ->
                // Pressure atm
                Component.translatable("tfg.tooltip.environment.pressure_atm", String.format("%.2f", pressure));
            case 2 ->
                // Pressure pascal
                Component.translatable("tfg.tooltip.environment.pressure_pascal", String.format("%.1f", pressure * 101.3F));
            case 3 ->
                // Pressure bar
                Component.translatable("tfg.tooltip.environment.pressure_bar", String.format("%.2f", pressure * 1.013F));
            case 4 ->
                // Gravity m/s/s
                Component.translatable("tfg.tooltip.environment.gravity_mss", String.format("%.1f", gravity));
            case 5 ->
                // Gravity b/s/s
                Component.translatable("tfg.tooltip.environment.gravity_bss", String.format("%.1f", gravity));
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
        return "environment";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (!isFirstLine) {
            builder.addSelectionScrollInput(0, 120, (si, l) -> si
                    .forOptions(CreateLang.translatedOptions("display_source.environment",
                            "oxygen",
                            "pressure_atm",
                            "pressure_pascal",
                            "pressure_bar",
                            "gravity_mss",
                            "gravity_bss",
                            "redstone_power"))
                    .titled(Component.translatable("tfg.display_source.environment.display_mode")), "Mode");
        }
    }
}
