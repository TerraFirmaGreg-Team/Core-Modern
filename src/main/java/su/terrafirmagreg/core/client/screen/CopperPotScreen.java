package su.terrafirmagreg.core.client.screen;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.PacketDistributor;

import dev.emi.emi.api.EmiApi;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.screen.widget.TemperatureMeterWidget;
import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.common.blockentity.appliances.CopperPotBlockEntity;
import su.terrafirmagreg.core.common.container.CopperPotContainer;
import su.terrafirmagreg.core.compat.emi.TFGEmiPlugin;
import su.terrafirmagreg.core.utils.TFGHelpers;

@SuppressWarnings({ "FieldCanBeLocal", "NoTranslation", "unused" })
public class CopperPotScreen extends BlockEntityScreen<CopperPotBlockEntity, CopperPotContainer> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/copper_pot.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 201;
    private static final int SLOT_SIZE = 18;

    private static final int TEMP_X = 4;
    private static final int TEMP_Y = 38;

    private static final int INPUT_TANKS_X = CopperPotContainer.INPUT_SLOT_X - 1;
    private static final int INPUT_TANKS_Y = CopperPotContainer.INPUT_SLOT_Y - 1 + (SLOT_SIZE * 2);
    private static final int TANK_HEIGHT = 31;
    private static final int TANK_WIDTH = 18;
    private static final int OUTPUT_TANK_OVERLAY_U = 176;
    private static final int OUTPUT_TANK_OVERLAY_V = 6;
    private static final int INPUT_TANK_OVERLAY_U = OUTPUT_TANK_OVERLAY_U;
    private static final int INPUT_TANK_OVERLAY_V = OUTPUT_TANK_OVERLAY_V + TANK_HEIGHT;
    private static final int TANK_HOVER_U = OUTPUT_TANK_OVERLAY_U + (TANK_WIDTH * 3);
    private static final int TANK_HOVER_V = OUTPUT_TANK_OVERLAY_V;

    private static final int PB_X = INPUT_TANKS_X + (TANK_WIDTH * 3) + 5;
    private static final int PB_Y = INPUT_TANKS_Y - 14;
    private static final int PB_U = OUTPUT_TANK_OVERLAY_U;
    private static final int PB_V = INPUT_TANK_OVERLAY_V + TANK_HEIGHT + 1;
    private static final int PB_WIDTH = 30;
    private static final int PB_HEIGHT = 32;

    public static final int OUTPUT_TANKS_X = PB_X + PB_WIDTH + 1;
    public static final int OUTPUT_TANKS_Y = PB_Y + 5;

    private static final int ERROR_X = PB_X + 8;
    private static final int ERROR_Y = PB_Y - 2;
    private static final int ERROR_U = OUTPUT_TANK_OVERLAY_U;
    private static final int ERROR_V = PB_V + PB_HEIGHT;
    private static final int ERROR_WIDTH = 9;
    private static final int ERROR_HEIGHT = 12;

    public CopperPotScreen(CopperPotContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 35;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // EMI Uses Button
        this.addRenderableWidget(Button.builder(Component.literal("Recipes"),
                (button) -> EmiApi.displayRecipeCategory(TFGEmiPlugin.COPPER_POT))
                .bounds(this.leftPos + PB_X, this.topPos + PB_Y - 2, PB_WIDTH, 11)
                .build(builder -> new Button(builder) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        // Override to prevent rendering a button texture.
                    }
                }));

        // Temp Meter Widget
        this.addRenderableWidget(new TemperatureMeterWidget(
                leftPos + TEMP_X,
                topPos + TEMP_Y,
                () -> (int) this.menu.getBlockEntity().getTemperature(),
                () -> (int) this.menu.getBlockEntity().getTargetTemperature()));
    }

    // Handles fluid tank interactions.
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            ItemStack carried = getMenu().getCarried();
            if (!carried.isEmpty() && carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
                // Check fluid input tanks.
                for (int i = 0; i < blockEntity.getFluidInputCount(); i++) {
                    int column = i % blockEntity.getFluidInputCount();
                    int currentTankX = INPUT_TANKS_X + (column * TANK_WIDTH);
                    if (RenderHelpers.isInside((int) mouseX, (int) mouseY, leftPos + currentTankX, topPos + INPUT_TANKS_Y, TANK_WIDTH, TANK_HEIGHT)) {
                        PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(CopperPotContainer.BUTTON_FLUID_INPUT_START + i, null));
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                        return true;
                    }
                }

                // Check fluid output tanks.
                for (int i = 0; i < blockEntity.getFluidOutputCount(); i++) {
                    int column = i % blockEntity.getFluidOutputCount();
                    int currentTankX = OUTPUT_TANKS_X + (column * TANK_WIDTH);
                    if (RenderHelpers.isInside((int) mouseX, (int) mouseY, leftPos + currentTankX, topPos + OUTPUT_TANKS_Y, TANK_WIDTH, TANK_HEIGHT)) {
                        PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(CopperPotContainer.BUTTON_FLUID_OUTPUT_START + i, null));
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        // =========== Fluid Input Tanks ==========

        // Input Tank Fill
        for (int i = 0; i < blockEntity.getFluidInputCount(); i++) {
            FluidStack fluidStack = blockEntity.getFluidInputTank(i);
            if (!fluidStack.isEmpty()) {
                int column = i % blockEntity.getFluidInputCount();
                int currentTankX = INPUT_TANKS_X + (column * TANK_WIDTH);

                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil((float) (TANK_HEIGHT - 2) * fluidStack.getAmount() / (float) blockEntity.getFluidInputCapacity());

                RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + currentTankX, topPos + INPUT_TANKS_Y + TANK_HEIGHT - fillHeight - 1, TANK_WIDTH, fillHeight, 16, 16);
                resetToBackgroundSprite();
            }
        }

        // Input Tanks Overlay
        graphics.blit(texture, leftPos + INPUT_TANKS_X, topPos + INPUT_TANKS_Y, INPUT_TANK_OVERLAY_U, INPUT_TANK_OVERLAY_V, TANK_WIDTH * 3, TANK_HEIGHT);

        // Input Tank Hover Effect
        for (int i = 0; i < blockEntity.getFluidInputCount(); i++) {
            int column = i % blockEntity.getFluidInputCount();
            int currentTankX = INPUT_TANKS_X + (column * TANK_WIDTH);

            if (RenderHelpers.isInside(mouseX, mouseY, leftPos + currentTankX, topPos + INPUT_TANKS_Y, TANK_WIDTH, TANK_HEIGHT)) {

                graphics.blit(texture, leftPos + currentTankX, topPos + INPUT_TANKS_Y, TANK_HOVER_U, TANK_HOVER_V, TANK_WIDTH, TANK_HEIGHT);
                break;
            }
        }

        // =========== Fluid Output Tanks ==========

        // Output Tank Fill
        for (int i = 0; i < blockEntity.getFluidOutputCount(); i++) {
            FluidStack fluidStack = blockEntity.getFluidOutputTank(i);
            if (!fluidStack.isEmpty()) {
                int column = i % blockEntity.getFluidOutputCount();
                int currentTankX = OUTPUT_TANKS_X + (column * TANK_WIDTH);

                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil((float) (TANK_HEIGHT - 2) * fluidStack.getAmount() / (float) blockEntity.getFluidOutputCapacity());

                RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + currentTankX, topPos + OUTPUT_TANKS_Y + TANK_HEIGHT - fillHeight - 1, TANK_WIDTH, fillHeight, 16, 16);
                resetToBackgroundSprite();
            }
        }

        // Output Tanks Overlay
        graphics.blit(texture, leftPos + OUTPUT_TANKS_X, topPos + OUTPUT_TANKS_Y, OUTPUT_TANK_OVERLAY_U, OUTPUT_TANK_OVERLAY_V, TANK_WIDTH * 3, TANK_HEIGHT);

        // Output Tank Hover Effect
        for (int i = 0; i < blockEntity.getFluidOutputCount(); i++) {
            int column = i % blockEntity.getFluidOutputCount();
            int currentTankX = OUTPUT_TANKS_X + (column * TANK_WIDTH);

            if (RenderHelpers.isInside(mouseX, mouseY, leftPos + currentTankX, topPos + OUTPUT_TANKS_Y, TANK_WIDTH, TANK_HEIGHT)) {

                graphics.blit(texture, leftPos + currentTankX, topPos + OUTPUT_TANKS_Y, TANK_HOVER_U, TANK_HOVER_V, TANK_WIDTH, TANK_HEIGHT);
                break;
            }
        }

        int cookingTicks = blockEntity.getCookingTicks();
        int maxDuration = blockEntity.getMaxDuration();
        if (cookingTicks > 0 && maxDuration > 0) {
            float progressPercentage = (float) cookingTicks / (float) maxDuration;
            int filledWidth = Math.round(PB_WIDTH * progressPercentage);

            if (filledWidth > 0) {
                graphics.blit(texture, leftPos + PB_X, topPos + PB_Y, PB_U, PB_V, filledWidth, PB_HEIGHT);
            }
        }

        if (blockEntity.getRecipeError() != CookingApplianceBlockEntity.CookingError.NONE) {
            graphics.blit(texture, leftPos + ERROR_X, topPos + ERROR_Y, ERROR_U, ERROR_V, ERROR_WIDTH, ERROR_HEIGHT);
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        // Fluid Input Tooltips
        for (int i = 0; i < blockEntity.getFluidInputCount(); i++) {
            int column = i % blockEntity.getFluidInputCount();

            int currentTankX = INPUT_TANKS_X + (column * TANK_WIDTH);

            if (RenderHelpers.isInside(mouseX, mouseY, leftPos + currentTankX, topPos + INPUT_TANKS_Y, TANK_WIDTH, TANK_HEIGHT)) {

                FluidStack fluid = blockEntity.getFluidInputTank(i);

                if (!fluid.isEmpty()) {
                    graphics.renderTooltip(font, Tooltips.fluidUnitsAndCapacityOf(fluid, blockEntity.getFluidInputCapacity()), mouseX, mouseY);
                }
                break;
            }
        }

        // Fluid Output Tooltips
        for (int i = 0; i < blockEntity.getFluidOutputCount(); i++) {
            int column = i % blockEntity.getFluidOutputCount();

            int currentTankX = OUTPUT_TANKS_X + (column * TANK_WIDTH);

            if (RenderHelpers.isInside(mouseX, mouseY, leftPos + currentTankX, topPos + OUTPUT_TANKS_Y, TANK_WIDTH, TANK_HEIGHT)) {

                FluidStack fluid = blockEntity.getFluidOutputTank(i);

                if (!fluid.isEmpty()) {
                    graphics.renderTooltip(font, Tooltips.fluidUnitsAndCapacityOf(fluid, blockEntity.getFluidOutputCapacity()), mouseX, mouseY);
                }
                break;
            }
        }

        // Progress Tooltips
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + PB_X, topPos + PB_Y - 2, PB_WIDTH - 1, 10)) {
            List<Component> progressTooltip = new ArrayList<>(List.of());

            MutableComponent openRecipes = Component.translatable("emi.view_all_recipes");
            progressTooltip.add(openRecipes);

            CookingApplianceBlockEntity.CookingError error = blockEntity.getRecipeError();
            if (error != CookingApplianceBlockEntity.CookingError.NONE) {
                progressTooltip.add(Component.empty());
                progressTooltip.add(error.getTooltipComponent());
            }

            if (blockEntity.getCookingTicks() > 0) {
                progressTooltip.add(Component.empty());
                progressTooltip.add(TFGHelpers.formattedTimer(blockEntity.getCookingTicks(), blockEntity.getMaxDuration()));
            }

            graphics.renderComponentTooltip(font, progressTooltip, mouseX, mouseY);
        }
    }
}
