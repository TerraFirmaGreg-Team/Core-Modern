package su.terrafirmagreg.core.client.screen;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

import su.terrafirmagreg.core.common.blockentity.GasBurnerBlockEntity;
import su.terrafirmagreg.core.common.container.GasBurnerBlockContainer;

public class GasBurnerScreen extends BlockEntityScreen<GasBurnerBlockEntity, GasBurnerBlockContainer> {
    private static final ResourceLocation FORGE = Helpers.identifier("textures/gui/charcoal_forge.png");

    public GasBurnerScreen(GasBurnerBlockContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, FORGE);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);
        int temp = Heat.scaleTemperatureForGui(blockEntity.getTemperature());
        if (temp > 0) {
            graphics.blit(texture, leftPos + 8, topPos + 76 - Math.min(51, temp), 176, 0, 15, 5);
        }

        blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
            FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            if (!fluidStack.isEmpty()) {
                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil((float) 50 * fluidStack.getAmount() / (float) GasBurnerBlockEntity.CAPACITY);

                RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + 152, topPos + 70 - fillHeight, 16, fillHeight, 16, 16);

                resetToBackgroundSprite();
            }
        });
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 8, topPos + 76 - 51, 15, 51)) {
            final var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getTemperature());
            if (text != null) {
                graphics.renderTooltip(font, text, mouseX, mouseY);
            }
        }

        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 152, topPos + 20, 16, 50)) {
            blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
                FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (!fluid.isEmpty()) {
                    graphics.renderTooltip(font, Tooltips.fluidUnitsAndCapacityOf(fluid, GasBurnerBlockEntity.CAPACITY), mouseX, mouseY);
                }
            });
        }
    }
}
