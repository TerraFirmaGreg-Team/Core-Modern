package su.terrafirmagreg.core.compat.jade;

import net.dries007.tfc.common.blockentities.BellowsBlockEntity;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.appliances.FireBoxBlock;
import su.terrafirmagreg.core.common.blockentity.appliances.FireBoxBlockEntity;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;
import su.terrafirmagreg.core.utils.TFGHelpers;

@SuppressWarnings("NoTranslation")
public class FireBoxProvider implements IBlockComponentProvider {

    public static final FireBoxProvider INSTANCE = new FireBoxProvider();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "fire_box_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof FireBoxBlockEntity blockEntity) {

            // Side-Based Power Display.
            int power = FireBoxBlock.getInputSignal(accessor.getLevel(), accessor.getPosition(), accessor.getSide());
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

            // Air Ticks
            int airTicks = blockEntity.getAirTicks();
            int maxAirTicks = BellowsBlockEntity.MAX_DEVICE_AIR_TICKS * 2;

            tooltip.add(Component.literal("༄ ").append(TFGHelpers.formattedPipTimer(airTicks, maxAirTicks, 10, ChatFormatting.GRAY)));

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
