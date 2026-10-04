package su.terrafirmagreg.core.compat.jade;

import net.dries007.tfc.config.TFCConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.GasBurnerBlock;
import su.terrafirmagreg.core.common.blockentity.GasBurnerBlockEntity;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

@SuppressWarnings("NoTranslation")
public class GasBurnerProvider implements IBlockComponentProvider {

    public static final GasBurnerProvider INSTANCE = new GasBurnerProvider();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "gas_burner_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof GasBurnerBlockEntity blockEntity) {

            // Max Temp Display.
            if (blockEntity.getMaxTemperature() > 0) {
                float maxTemp = blockEntity.getMaxTemperature();
                MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(maxTemp);

                if (formattedTemp != null) {
                    Component maxTempText = Component.translatable("tfg.tooltip.gas_burner.max_temp").append(formattedTemp);
                    tooltip.add(maxTempText);
                }
            }

            // Side-Based Power Display.
            int power = GasBurnerBlock.getInputSignal(accessor.getLevel(), accessor.getPosition(), accessor.getSide());
            if (power > 0) {
                MutableComponent powerText = Component.translatable("tfg.tooltip.unit.power", power);
                tooltip.add(powerText);
            }

            if (blockEntity.getBurnTicks() > 0) {
                // Timer Display.
                float remainingSeconds = (float) blockEntity.getBurnTicks() / 20;
                MutableComponent timerIcon = ((int) remainingSeconds % 2 != 0) ? Component.literal("⏳ ") : Component.literal("⌛ ");
                Component timerText = timerIcon.append(Component.translatable("tfg.tooltip.unit.seconds", String.format("%.1f", remainingSeconds)));

                tooltip.add(timerText);

                // Current Temp Display.
                float temp = blockEntity.getTemperature();
                MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temp);

                if (formattedTemp != null) {
                    Component tempText = Component.literal("\uD83D\uDD25 ").append(formattedTemp);
                    tooltip.add(tempText);
                }
            }

            // Oxygenated Display.
            if (!EnvironmentSystem.hasOxygen(accessor.getLevel(), accessor.getPosition())) {
                tooltip.add(Component.translatable("tfg.tooltip.firmalife_greenhouse.oxygen_required"));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
