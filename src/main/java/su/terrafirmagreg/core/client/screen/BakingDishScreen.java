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
import su.terrafirmagreg.core.client.screen.widget.SealButton;
import su.terrafirmagreg.core.common.block.appliances.BakingDishBlock;
import su.terrafirmagreg.core.common.blockentity.appliances.BakingDishBlockEntity;
import su.terrafirmagreg.core.common.container.BakingDishContainer;
import su.terrafirmagreg.core.compat.emi.TFGEmiPlugin;

@SuppressWarnings({ "FieldCanBeLocal", "unused" })
public class BakingDishScreen extends BlockEntityScreen<BakingDishBlockEntity, BakingDishContainer> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/baking_dish.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 177;
    private static final int SLOT_SIZE = 18;

    private static final int INPUT_TANK_X = BakingDishContainer.INPUT_SLOT_X + (SLOT_SIZE * 3) - 1;
    private static final int INPUT_TANK_Y = BakingDishContainer.INPUT_SLOT_Y - 1;
    private static final int TANK_HEIGHT = 36;
    private static final int TANK_WIDTH = 18;
    private static final int INPUT_TANK_OVERLAY_U = WIDTH;
    private static final int INPUT_TANK_OVERLAY_V = 0;
    private static final int TANK_HOVER_U = INPUT_TANK_OVERLAY_U + TANK_WIDTH;
    private static final int TANK_HOVER_V = INPUT_TANK_OVERLAY_V;

    private static final int SEAL_BUTTON_X = INPUT_TANK_X + (TANK_WIDTH * 2) + 5;
    private static final int SEAL_BUTTON_Y = INPUT_TANK_Y + (TANK_HEIGHT / 2) - (SealButton.SIZE / 2);

    private boolean isSealed() {
        return blockEntity.getBlockState().getValue(BakingDishBlock.SEALED);
    }

    public BakingDishScreen(BakingDishContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 11;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // EMI Uses Button
        this.addRenderableWidget(Button.builder(Component.literal("Recipes"),
                (button) -> EmiApi.displayRecipeCategory(TFGEmiPlugin.BAKING_DISH))
                .bounds(this.leftPos + 3, this.topPos + 3, Minecraft.getInstance().font.width(this.title.getVisualOrderText()), 11)
                .build(builder -> new Button(builder) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        // Override to prevent rendering a button texture.
                    }
                }));

        addRenderableWidget(new SealButton(this.blockEntity, getGuiLeft() + SEAL_BUTTON_X, getGuiTop() + SEAL_BUTTON_Y));
    }

    // Handles fluid tank interactions.
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1) && !isSealed()) {
            ItemStack carried = getMenu().getCarried();
            if (!carried.isEmpty() && carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
                if (RenderHelpers.isInside((int) mouseX, (int) mouseY, leftPos + INPUT_TANK_X, topPos + INPUT_TANK_Y, TANK_WIDTH, TANK_HEIGHT)) {
                    PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(BakingDishContainer.BUTTON_FLUID_IDX, null));
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        // Input Tank Fill
        FluidStack fluidStack = blockEntity.getFluidTank();
        if (!fluidStack.isEmpty()) {

            final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
            final int fillHeight = (int) Math.ceil((float) (TANK_HEIGHT - 2) * fluidStack.getAmount() / (float) blockEntity.getFluidCapacity());

            RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + INPUT_TANK_X, topPos + INPUT_TANK_Y + TANK_HEIGHT - fillHeight - 1, TANK_WIDTH, fillHeight, 16, 16);
            resetToBackgroundSprite();
        }

        // Input Tanks Overlay
        graphics.blit(texture, leftPos + INPUT_TANK_X, topPos + INPUT_TANK_Y, INPUT_TANK_OVERLAY_U, INPUT_TANK_OVERLAY_V, TANK_WIDTH, TANK_HEIGHT);

        // Input Tank Hover Effect
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + INPUT_TANK_X, topPos + INPUT_TANK_Y, TANK_WIDTH, TANK_HEIGHT)) {
            graphics.blit(texture, leftPos + INPUT_TANK_X, topPos + INPUT_TANK_Y, TANK_HOVER_U, TANK_HOVER_V, TANK_WIDTH, TANK_HEIGHT);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        // Sealed Overlay
        if (isSealed()) {
            graphics.fill(INPUT_TANK_X + 1, INPUT_TANK_Y + 1, INPUT_TANK_X + TANK_WIDTH - 1, INPUT_TANK_Y + TANK_HEIGHT - 1, -2130706433);
            drawDisabled(graphics, 0, BakingDishBlockEntity.ITEM_SLOTS - 1);
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        // Fluid Input Tooltip
        FluidStack fluid = blockEntity.getFluidTank();
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + INPUT_TANK_X, topPos + INPUT_TANK_Y, TANK_WIDTH, TANK_HEIGHT)) {

            if (!fluid.isEmpty()) {
                graphics.renderTooltip(font, Tooltips.fluidUnitsAndCapacityOf(fluid, blockEntity.getFluidCapacity()), mouseX, mouseY);
            }
        }

        // EMI Tooltip
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 3, topPos + 3, Minecraft.getInstance().font.width(this.title.getVisualOrderText()), 11)) {
            List<Component> recipeTooltip = new ArrayList<>(List.of());

            MutableComponent openRecipes = Component.translatable("emi.view_all_recipes");
            recipeTooltip.add(openRecipes);

            graphics.renderComponentTooltip(font, recipeTooltip, mouseX, mouseY);
        }
    }
}
