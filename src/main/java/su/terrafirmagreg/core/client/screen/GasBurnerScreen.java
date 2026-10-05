package su.terrafirmagreg.core.client.screen;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.blockentity.GasBurnerBlockEntity;
import su.terrafirmagreg.core.common.container.GasBurnerBlockContainer;

@SuppressWarnings("FieldCanBeLocal")
public class GasBurnerScreen extends BlockEntityScreen<GasBurnerBlockEntity, GasBurnerBlockContainer> {
    private static final ResourceLocation BURNER = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/gas_burner.png");

    private final int tempScaleX = 25;
    private final int tempScaleY = 76;
    private final int tempScaleHeight = 51;

    private final int currentTempBarU = 176;
    private final int currentTempBarV = 0;
    private final int currentTempBarWidth = 15;
    private final int currentTempBarHeight = 5;

    private final int maxTempBarU = currentTempBarU;
    private final int maxTempBarV = currentTempBarV + currentTempBarHeight;
    private final int maxTempBarWidth = currentTempBarWidth;
    private final int maxTempBarHeight = currentTempBarHeight;

    private final int flameX = 63;
    private final int flameY = 22;
    private final int flameU = currentTempBarU;
    private final int flameV = maxTempBarV + maxTempBarHeight;
    private final int flameWidth = 21;
    private final int flameHeight = 32;

    private final int tankX = 108;
    private final int tankY = 26;
    private final int tankHeight = 50;
    private final int tankWidth = 16;
    private final int tankOverlayU = currentTempBarU;
    private final int tankOverlayV = flameV + flameHeight;

    public GasBurnerScreen(GasBurnerBlockContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, BURNER);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        // Max Temperature Indicator.
        int maxTemp = Heat.scaleTemperatureForGui(blockEntity.getMaxTemperature());
        if (maxTemp > 0) {
            graphics.fill(leftPos + tempScaleX + 3, topPos + tempScaleY - tempScaleHeight, leftPos + tempScaleX + maxTempBarWidth - 3, topPos + tempScaleY - Math.min(tempScaleHeight, maxTemp),
                    0xD53A274A);
            graphics.blit(texture, leftPos + tempScaleX, topPos + tempScaleY - Math.min(tempScaleHeight, maxTemp) - 3, maxTempBarU, maxTempBarV, maxTempBarWidth, maxTempBarHeight);
        }

        // Temperature Indicator.
        int temp = Heat.scaleTemperatureForGui(blockEntity.getTemperature());
        if (temp > 0) {
            graphics.blit(texture, leftPos + tempScaleX, topPos + tempScaleY - Math.min(tempScaleHeight, temp), currentTempBarU, currentTempBarV, currentTempBarWidth, currentTempBarHeight);
        }

        // Fluid Tank.
        blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
            FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            if (!fluidStack.isEmpty()) {
                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil((float) tankHeight * fluidStack.getAmount() / (float) GasBurnerBlockEntity.CAPACITY);

                RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + tankX, topPos + tankY + tankHeight - fillHeight, tankWidth, fillHeight, 16, 16);

                resetToBackgroundSprite();
            }
        });
        graphics.blit(texture, leftPos + tankX - 1, topPos + tempScaleY - tempScaleHeight, tankOverlayU, tankOverlayV, tankWidth + 2, tankHeight + 2);

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
        int maxTemp = Heat.scaleTemperatureForGui(blockEntity.getMaxTemperature());

        // Max Temperature Indicator Tooltip.
        if (maxTemp > 0 && RenderHelpers.isInside(mouseX, mouseY, leftPos + tempScaleX, topPos + tempScaleY - Math.min(tempScaleHeight, maxTemp) - 3, currentTempBarWidth, maxTempBarHeight)) {
            final var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getMaxTemperature());
            if (text != null) {
                graphics.renderTooltip(font, text, mouseX, mouseY);
            }
            // Temperature Indicator Tooltip.
        } else if (RenderHelpers.isInside(mouseX, mouseY, leftPos + tempScaleX, topPos + tempScaleY - tempScaleHeight, currentTempBarWidth, tempScaleHeight)) {
            final var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getTemperature());
            if (text != null) {
                graphics.renderTooltip(font, text, mouseX, mouseY);
            }
        }

        // Fluid Tooltip.
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + tankX, topPos + tankY, tankWidth, tankHeight)) {
            blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
                FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (!fluid.isEmpty()) {
                    graphics.renderTooltip(font, Tooltips.fluidUnitsAndCapacityOf(fluid, GasBurnerBlockEntity.CAPACITY), mouseX, mouseY);
                }
            });
        }

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
