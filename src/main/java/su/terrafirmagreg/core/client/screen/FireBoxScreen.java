package su.terrafirmagreg.core.client.screen;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.screen.widget.TemperatureMeterWidget;
import su.terrafirmagreg.core.common.blockentity.appliances.FireBoxBlockEntity;
import su.terrafirmagreg.core.common.container.FireBoxContainer;

@SuppressWarnings({ "FieldCanBeLocal", "NoTranslation" })
public class FireBoxScreen extends BlockEntityScreen<FireBoxBlockEntity, FireBoxContainer> {
    private static final ResourceLocation FIRE_BOX = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/fire_box.png");

    private final int tempScaleX = 32;
    private final int tempScaleY = 28;

    private final int flameX = 80;
    private final int flameY = 27;
    private final int flameU = 176;
    private final int flameV = 0;
    private final int flameWidth = 13;
    private final int flameHeight = 29;

    @Override
    protected void init() {
        super.init();

        // Temp Meter
        this.addRenderableWidget(new TemperatureMeterWidget(
                leftPos + tempScaleX,
                topPos + tempScaleY,
                () -> (int) this.menu.getBlockEntity().getTemperature(),
                () -> (int) this.menu.getBlockEntity().burnTemperature,
                null));
    }

    public FireBoxScreen(FireBoxContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, FIRE_BOX);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        // Flame Progress Bar
        int burnTicks = blockEntity.getBurnTicks();
        int burnTime = blockEntity.getMaxBurnTicks() > 0 ? blockEntity.getMaxBurnTicks() : 1000;
        if (burnTicks > 0) {
            float smoothBurnTicks = burnTicks - partialTicks;
            float burnPercentage = smoothBurnTicks / (float) burnTime;
            int remainingHeight = Math.round(flameHeight * burnPercentage);

            if (remainingHeight > 0) {
                int burnOffset = flameHeight - remainingHeight;
                graphics.blit(texture, leftPos + flameX, topPos + flameY + burnOffset, flameU, flameV + burnOffset, flameWidth, remainingHeight);
            }
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        // Timer Tooltip.
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + flameX, topPos + flameY, flameWidth, flameHeight)) {
            if (blockEntity.getBurnTicks() > 0) {
                float remainingSeconds = (float) blockEntity.getBurnTicks() / 20;
                MutableComponent timerIcon = ((int) remainingSeconds % 2 != 0) ? Component.literal("⏳ ") : Component.literal("⌛ ");
                Component timerText = timerIcon.append(Component.translatable("tfg.tooltip.unit.seconds", String.format("%.1f", remainingSeconds)));
                float temp = blockEntity.burnTemperature;
                MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temp);
                if (formattedTemp == null) {
                    formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatRange(temp);
                }
                if (formattedTemp != null) {
                    Component tempText = Component.literal("\uD83D\uDD25 ").append(formattedTemp);
                    graphics.renderComponentTooltip(font, List.of(timerText, tempText), mouseX, mouseY);
                } else {
                    graphics.renderComponentTooltip(font, List.of(timerText), mouseX, mouseY);
                }
            }
        }
    }
}
