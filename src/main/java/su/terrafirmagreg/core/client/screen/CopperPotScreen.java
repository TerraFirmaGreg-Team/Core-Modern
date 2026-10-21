package su.terrafirmagreg.core.client.screen;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.blockentity.CopperPotBlockEntity;
import su.terrafirmagreg.core.common.container.CopperPotContainer;

@SuppressWarnings("FieldCanBeLocal")
public class CopperPotScreen extends BlockEntityScreen<CopperPotBlockEntity, CopperPotContainer> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/copper_pot.png");

    private final int tempScaleX = 2;
    private final int tempScaleY = 76;
    private final int tempScaleHeight = 51;

    private final int currentTempBarU = 176;
    private final int currentTempBarV = 0;
    private final int currentTempBarWidth = 15;
    private final int currentTempBarHeight = 5;

//    private final int flameX = 63;
//    private final int flameY = 22;
//    private final int flameU = currentTempBarU;
//    private final int flameV = currentTempBarV + currentTempBarHeight * 2;
//    private final int flameWidth = 21;
//    private final int flameHeight = 32;

    private final int tankX = 44 - 18;
    private final int tankY = 25 + 30;
    private final int tankHeight = 30;
    private final int tankWidth = 16;
    private final int tankOverlayU = currentTempBarU;
    private final int tankOverlayV = currentTempBarV + tankHeight + 7;

    public CopperPotScreen(CopperPotContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelX += 110;
        inventoryLabelY += 20;
        titleLabelX += 105;
        imageHeight += 20;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        // Temperature Bar
        int temp = Heat.scaleTemperatureForGui(blockEntity.getTemperature());
        if (temp > 0) {
            graphics.blit(texture, leftPos + tempScaleX, topPos + tempScaleY - Math.min(tempScaleHeight, temp), currentTempBarU, currentTempBarV, currentTempBarWidth, currentTempBarHeight);
        }

        // Fluid Input Tanks
        for (int i = 0; i < blockEntity.getFluidInputCount(); i++) {
            FluidStack fluidStack = blockEntity.getFluidInputTank(i);
            if (!fluidStack.isEmpty()) {
                int column = i % 3;
                int row = i / 3;
                int currentTankX = tankX + (column * (tankWidth + 2));
                int currentTankY = tankY + (row * tankHeight);

                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil((float) tankHeight * fluidStack.getAmount() / (float) blockEntity.getFluidInputCapacity());

                RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + currentTankX, topPos + currentTankY + tankHeight - fillHeight, tankWidth, fillHeight, 16, 16);
                resetToBackgroundSprite();
            }
        }

        graphics.blit(texture, leftPos + tankX - 1, topPos + tankY, tankOverlayU, tankOverlayV, (tankWidth + 2) * 3, tankHeight + 1);

        for (int i = 0; i < blockEntity.getFluidInputCount(); i++) {
            int column = i % 3;
            int row = i / 3;
            int currentTankX = tankX + (column * (tankWidth + 2));
            int currentTankY = tankY + (row * tankHeight);

            if (RenderHelpers.isInside(mouseX, mouseY, leftPos + currentTankX, topPos + currentTankY, tankWidth, tankHeight)) {

                graphics.blit(texture, leftPos + currentTankX - 1, topPos + currentTankY, tankOverlayU + (tankWidth + 2) * 3, currentTempBarV + 6, tankWidth + 2, tankHeight + 2);
                break;
            }
        }

        // Fluid Output Tanks
        for (int i = 0; i < blockEntity.getFluidOutputCount(); i++) {
            FluidStack fluidStack = blockEntity.getFluidOutputTank(i);
            if (!fluidStack.isEmpty()) {
                int column = i % 3;
                int row = i / 3;
                int currentTankX = tankX + 90 + (column * (tankWidth + 2));
                int currentTankY = tankY - 7 + (row * tankHeight);

                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil((float) tankHeight * fluidStack.getAmount() / (float) blockEntity.getFluidOutputCapacity());

                RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + currentTankX, topPos + currentTankY + tankHeight - fillHeight, tankWidth, fillHeight, 16, 16);
                resetToBackgroundSprite();
            }
        }

        graphics.blit(texture, leftPos + tankX + 90 - 1, topPos + tankY  - 7, tankOverlayU, currentTempBarV + 6, (tankWidth + 2) * 3, tankHeight + 1);

        for (int i = 0; i < blockEntity.getFluidInputCount(); i++) {
            int column = i % 3;
            int row = i / 3;
            int currentTankX = tankX + 90 + (column * (tankWidth + 2));
            int currentTankY = tankY - 7 + (row * tankHeight);

            if (RenderHelpers.isInside(mouseX, mouseY, leftPos + currentTankX, topPos + currentTankY, tankWidth, tankHeight)) {

                graphics.blit(texture, leftPos + currentTankX - 1, topPos + currentTankY, tankOverlayU + (tankWidth + 2) * 3, currentTempBarV + 6, tankWidth + 2, tankHeight + 2);
                break;
            }
        }

        //        int cookingTicks = blockEntity.getCookingTicks();
        //        int maxDuration = blockEntity.getMaxDuration();
        //        if (cookingTicks > 0 && maxDuration > 0) {
        //            float progressPercentage = (float) cookingTicks / (float) maxDuration;
        //            int filledHeight = Math.round(flameHeight * progressPercentage);
        //
        //            if (filledHeight > 0) {
        //                int burnOffset = flameHeight - filledHeight;
        //                graphics.blit(texture, leftPos + flameX, topPos + flameY + burnOffset, flameU, flameV + burnOffset, flameWidth, filledHeight);
        //            }
        //        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        // Temperature Indicator Tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + tempScaleX, topPos + tempScaleY - tempScaleHeight, currentTempBarWidth, tempScaleHeight)) {
            final var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getTemperature());
            if (text != null) {
                graphics.renderTooltip(font, text, mouseX, mouseY);
            }
        }

        // Fluid Input Tooltips
        for (int i = 0; i < blockEntity.getFluidInputCount(); i++) {
            int column = i % 3;
            int row = i / 3;

            int currentTankX = tankX + (column * (tankWidth + 2));
            int currentTankY = tankY + (row * tankHeight);

            if (RenderHelpers.isInside(mouseX, mouseY, leftPos + currentTankX, topPos + currentTankY, tankWidth, tankHeight)) {

                FluidStack fluid = blockEntity.getFluidInputTank(i);

                if (!fluid.isEmpty()) {
                    graphics.renderTooltip(font, Tooltips.fluidUnitsAndCapacityOf(fluid, blockEntity.getFluidInputCapacity()), mouseX, mouseY);
                }
                break;
            }
        }

        // Fluid Output Tooltips
        for (int i = 0; i < blockEntity.getFluidOutputCount(); i++) {
            int column = i % 3;
            int row = i / 3;

            int currentTankX = tankX + 90 + (column * (tankWidth + 2));
            int currentTankY = tankY - 7 + (row * tankHeight);

            if (RenderHelpers.isInside(mouseX, mouseY, leftPos + currentTankX, topPos + currentTankY, tankWidth, tankHeight)) {

                FluidStack fluid = blockEntity.getFluidOutputTank(i);

                if (!fluid.isEmpty()) {
                    graphics.renderTooltip(font, Tooltips.fluidUnitsAndCapacityOf(fluid, blockEntity.getFluidOutputCapacity()), mouseX, mouseY);
                }
                break;
            }
        }

        // Progress Tooltip
        //        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + flameX, topPos + flameY, flameWidth, flameHeight)) {
        //            if (blockEntity.getCookingTicks() > 0) {
        //                int remainingTicks = Math.max(0, blockEntity.getMaxDuration() - blockEntity.getCookingTicks());
        //                float remainingSeconds = (float) remainingTicks / 20.0f;
        //                MutableComponent timerIcon = ((int) remainingSeconds % 2 != 0) ? Component.literal("⏳ ") : Component.literal("⌛ ");
        //                Component timerText = timerIcon.append(Component.translatable("tfg.tooltip.unit.seconds", String.format("%.1f", remainingSeconds)));
        //                graphics.renderComponentTooltip(font, List.of(timerText), mouseX, mouseY);
        //            }
        //        }
    }
}
