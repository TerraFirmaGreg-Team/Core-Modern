package su.terrafirmagreg.core.client.screen.widget;

import java.util.*;
import java.util.function.IntSupplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

/**
 * Widget for displaying temperature for TFC {@link Heat} capable devices.
 * Register in init(). Example {@link su.terrafirmagreg.core.client.screen.GasBurnerScreen}
 */
@SuppressWarnings({ "unused", "UnusedAssignment", "NoTranslation" })
public class TemperatureMeterWidget extends AbstractWidget {
    public static final ResourceLocation TEMP_METER = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/widgets/temperature_widget.png");
    private static final int TEXTURE_SIZE = 64;

    private static final int METER_U = 0;
    private static final int METER_V = 0;
    public static final int METER_WIDTH = 12;
    public static final int METER_HEIGHT = 53;

    private static final int INDICATOR_U = METER_U + METER_WIDTH;
    private static final int INDICATOR_V = 0;
    private static final int INDICATOR_WIDTH = 11;
    private static final int INDICATOR_HEIGHT = 5;

    private static final int TARGET_U = INDICATOR_U;
    private static final int TARGET_V = INDICATOR_V + INDICATOR_HEIGHT;
    private static final int TARGET_WIDTH = 4;
    private static final int TARGET_HEIGHT = 5;

    private static final int LOCK_U = INDICATOR_U;
    private static final int LOCK_V = TARGET_V + TARGET_HEIGHT;
    private static final int LOCK_WIDTH = 14;
    private static final int LOCK_HEIGHT = 3;

    private final IntSupplier currentTempSupplier;
    @Nullable
    private final IntSupplier targetTempSupplier;
    @Nullable
    private final IntSupplier maxTempSupplier;

    /**
     * Temperature meter widget with current, target, and max temp indicators.
     * @param x X-position.
     * @param y Y-position.
     * @param currentTempSupplier Current temperature in Celsius.
     * @param targetTempSupplier Target temperature in Celsius.
     * @param maxTempSupplier Maximum temperature in Celsius. For locking the temperature meter.
     */
    public TemperatureMeterWidget(int x, int y, IntSupplier currentTempSupplier, @Nullable IntSupplier targetTempSupplier, @Nullable IntSupplier maxTempSupplier) {
        super(x, y, METER_WIDTH, METER_HEIGHT, Component.empty());
        this.currentTempSupplier = currentTempSupplier;
        this.targetTempSupplier = targetTempSupplier;
        this.maxTempSupplier = maxTempSupplier;
    }

    /**
     * Temperature meter widget with current, target temp indicators.
     * @param x X-position.
     * @param y Y-position.
     * @param currentTempSupplier Current temperature in Celsius.
     * @param targetTempSupplier Target temperature in Celsius.
     */
    public TemperatureMeterWidget(int x, int y, IntSupplier currentTempSupplier, @Nullable IntSupplier targetTempSupplier) {
        super(x, y, METER_WIDTH, METER_HEIGHT, Component.empty());
        this.currentTempSupplier = currentTempSupplier;
        this.targetTempSupplier = targetTempSupplier;
        this.maxTempSupplier = null;
    }

    /**
     * Temperature meter widget with current temp indicator.
     * @param x X-position.
     * @param y Y-position.
     * @param currentTempSupplier Current temperature in Celsius.
     */
    public TemperatureMeterWidget(int x, int y, IntSupplier currentTempSupplier) {
        super(x, y, METER_WIDTH, METER_HEIGHT, Component.empty());
        this.currentTempSupplier = currentTempSupplier;
        this.targetTempSupplier = null;
        this.maxTempSupplier = null;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(TEMP_METER, getX(), getY(), METER_U, METER_V, METER_WIDTH, METER_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Max temp lock.
        int rawMaxTemp = 0;
        int maxTempScaled = 0;

        if (this.maxTempSupplier != null) {
            rawMaxTemp = maxTempSupplier.getAsInt();
            maxTempScaled = Heat.scaleTemperatureForGui(rawMaxTemp);

            if (maxTempScaled > 0) {
                int maxTempClamped = Math.min(METER_HEIGHT, maxTempScaled);

                // Background Fill
                graphics.fill(
                        getX() + 3,
                        getY() + 1,
                        getX() + METER_WIDTH - 1,
                        getY() + METER_HEIGHT - maxTempClamped,
                        0xD53A274A);

                // Lock Indicator Bar
                graphics.blit(TEMP_METER, getX() - 1, getY() + METER_HEIGHT - maxTempClamped - 1,
                        LOCK_U, LOCK_V, LOCK_WIDTH, LOCK_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }

        // Target Temp Indicator
        int rawTargetTemp = 0;
        int targetTempScaled = 0;

        if (this.targetTempSupplier != null) {
            rawTargetTemp = targetTempSupplier.getAsInt();
            targetTempScaled = Heat.scaleTemperatureForGui(rawTargetTemp);

            if (targetTempScaled > 0) {
                int targetTempClamped = Math.min(METER_HEIGHT, targetTempScaled);

                graphics.blit(TEMP_METER, getX() + METER_WIDTH - TARGET_WIDTH + 1, getY() + METER_HEIGHT - targetTempClamped - 2,
                        TARGET_U, TARGET_V, TARGET_WIDTH, TARGET_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }

        // Current Temp Indicator
        int rawCurrentTemp = 0;
        int currentTempScaled = 0;

        if (this.currentTempSupplier != null) {
            rawCurrentTemp = currentTempSupplier.getAsInt();
            currentTempScaled = Heat.scaleTemperatureForGui(rawCurrentTemp);

            if (currentTempScaled > 0) {
                int currentTempClamped = Math.min(METER_HEIGHT, currentTempScaled);

                graphics.blit(TEMP_METER, getX() - 1, getY() + METER_HEIGHT - currentTempClamped - 2,
                        INDICATOR_U, INDICATOR_V, INDICATOR_WIDTH, INDICATOR_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }

        renderTooltips(graphics, mouseX, mouseY, maxTempScaled, rawMaxTemp, rawCurrentTemp, rawTargetTemp);
    }

    /*
     Render temperature tooltips when hovering over the temperature meter.
     Holding shift will display the temperatures in unit measurements.
     */
    private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY, int maxTempScaled, int rawMaxTemp, int rawCurrentTemp, int rawTargetTemp) {
        var font = Minecraft.getInstance().font;
        var style = TFCConfig.CLIENT.heatTooltipStyle.get();
        var unit = TFCConfig.CLIENT.climateTooltipStyle.get();

        // Don't display anything if the device is off.
        if (style.formatColored(rawCurrentTemp) != null) {

            // Current Temp tooltip
            MutableComponent currentTemp = Component.empty();

            currentTemp = Component.literal("🔥 ").append(Objects.requireNonNull(style.formatColored(rawCurrentTemp)));

            if (Screen.hasShiftDown()) {
                currentTemp = Component.literal("🔥 ").append(Objects.requireNonNull(unit.format(rawCurrentTemp)));
            }

            List<Component> text = new ArrayList<>(List.of());
            text.add(currentTemp);

            // Target Temp tooltip
            if ((rawTargetTemp != 0 && Math.abs(rawTargetTemp - rawCurrentTemp) > 100) || rawMaxTemp != 0) {
                text.add(Component.empty());
            }

            // Target temp will not display if it is within 100 degrees of current temp.
            if (style.formatColored(rawTargetTemp) != null && rawTargetTemp != 0 && Math.abs(rawTargetTemp - rawCurrentTemp) > 100) {
                MutableComponent targetTemp = Component.empty()
                        .append(Component.translatable("tfg.tooltip.target").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Objects.requireNonNull(style.formatColored(rawTargetTemp)));

                if (Screen.hasShiftDown()) {
                    targetTemp = Component.empty()
                            .append(Component.translatable("tfg.tooltip.target").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Objects.requireNonNull(unit.format(rawTargetTemp)));
                }

                text.add(targetTemp);
            }

            // Max Temp Tooltip
            if (style.formatColored(rawMaxTemp) != null && rawMaxTemp != 0) {
                MutableComponent maxTemp = Component.empty()
                        .append(Component.translatable("tfg.tooltip.max").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Objects.requireNonNull(style.formatColored(rawMaxTemp)));

                if (Screen.hasShiftDown()) {
                    maxTemp = Component.empty()
                            .append(Component.translatable("tfg.tooltip.max").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Objects.requireNonNull(unit.format(rawMaxTemp)));
                }

                text.add(maxTemp);
            }

            if (isMouseOverScale(mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, text, mouseX, mouseY);
            }
        }

    }

    private boolean isMouseOverMaxBar(double mouseX, double mouseY, int maxTempScaled) {
        int maxTempClamped = Math.min(METER_HEIGHT, maxTempScaled);
        return mouseX >= getX() && mouseX < getX() + METER_WIDTH &&
                mouseY >= (getY() + METER_HEIGHT - maxTempClamped - 1) && mouseY < (getY() + METER_HEIGHT - maxTempClamped - 1 + LOCK_HEIGHT);
    }

    private boolean isMouseOverScale(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + METER_WIDTH &&
                mouseY >= getY() && mouseY < getY() + METER_HEIGHT;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }
}
