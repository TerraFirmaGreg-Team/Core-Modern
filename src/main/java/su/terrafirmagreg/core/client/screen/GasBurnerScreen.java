package su.terrafirmagreg.core.client.screen;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
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
import su.terrafirmagreg.core.client.screen.widget.TemperatureMeterWidget;
import su.terrafirmagreg.core.common.blockentity.GasBurnerBlockEntity;
import su.terrafirmagreg.core.common.container.GasBurnerBlockContainer;

@SuppressWarnings("FieldCanBeLocal")
public class GasBurnerScreen extends BlockEntityScreen<GasBurnerBlockEntity, GasBurnerBlockContainer> {
    private static final ResourceLocation BURNER = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/gas_burner.png");

    private final int tempScaleX = 25;
    private final int tempScaleY = 25;

    private final int flameX = 63;
    private final int flameY = 21;
    private final int flameU = 176;
    private final int flameV = 10;
    private final int flameWidth = 21;
    private final int flameHeight = 32;

    private final int tankX = 108;
    private final int tankY = 26;
    private final int tankHeight = 50;
    private final int tankWidth = 16;
    private final int tankOverlayU = 176;
    private final int tankOverlayV = flameV + flameHeight;

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(new TemperatureMeterWidget(
                leftPos + tempScaleX,
                topPos + tempScaleY,
                () -> (int) this.menu.getBlockEntity().getTemperature(),
                () -> (int) this.menu.getBlockEntity().burnTemperature,
                () -> (int) this.menu.getBlockEntity().getMaxTemperature()));
    }

    public GasBurnerScreen(GasBurnerBlockContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, BURNER);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

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
        graphics.blit(texture, leftPos + tankX - 1, topPos + 25, tankOverlayU, tankOverlayV, tankWidth + 2, tankHeight + 2);

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
