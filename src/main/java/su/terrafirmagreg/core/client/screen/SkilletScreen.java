package su.terrafirmagreg.core.client.screen;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import dev.emi.emi.api.EmiApi;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.screen.widget.TemperatureMeterWidget;
import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.common.blockentity.appliances.SkilletBlockEntity;
import su.terrafirmagreg.core.common.container.SkilletContainer;
import su.terrafirmagreg.core.compat.emi.TFGEmiPlugin;

@SuppressWarnings({ "FieldCanBeLocal", "NoTranslation", "unused" })
public class SkilletScreen extends BlockEntityScreen<SkilletBlockEntity, SkilletContainer> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/skillet.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 201;
    private static final int SLOT_SIZE = 18;

    private static final int TEMP_X = 4;
    private static final int TEMP_Y = 35;

    private static final int ERROR_X = SkilletContainer.CATALYST_SLOT_X - 4;
    private static final int ERROR_Y = SkilletContainer.CATALYST_SLOT_Y + SLOT_SIZE;
    private static final int ERROR_WIDTH = 9;
    private static final int ERROR_HEIGHT = 12;
    private static final int ERROR_HOVER_WIDTH = SLOT_SIZE * 2;
    private static final int ERROR_HOVER_HEIGHT = SLOT_SIZE - 3;
    private static final int ERROR_U = 176;
    private static final int ERROR_V = 0;

    private static final int PB_X = SkilletContainer.CATALYST_SLOT_X - 12;
    private static final int PB_Y = SkilletContainer.CATALYST_SLOT_Y + 4;
    private static final int PB_WIDTH = 46;
    private static final int PB_HEIGHT = 43;
    private static final int PB_U = ERROR_U;
    private static final int PB_V = ERROR_V + ERROR_HEIGHT;

    public SkilletScreen(SkilletContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 35;
        imageHeight = HEIGHT;
        imageWidth = WIDTH;
    }

    @Override
    protected void init() {
        super.init();

        // EMI Uses Button
        this.addRenderableWidget(Button.builder(Component.literal("Recipes"),
                (button) -> EmiApi.displayRecipeCategory(TFGEmiPlugin.SKILLET))
                .bounds(this.leftPos + ERROR_X, this.topPos + ERROR_Y, ERROR_HOVER_WIDTH + 1, ERROR_HOVER_HEIGHT)
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

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        // Progress Bar
        if (blockEntity.getCookingTicks() > 0) {
            int progress = (int) (PB_WIDTH * (float) blockEntity.getCookingTicks() / (float) blockEntity.getMaxDuration());
            graphics.blit(TEXTURE, leftPos + PB_X, topPos + PB_Y, PB_U, PB_V, progress, PB_HEIGHT);
        }

        // Cooking Errors
        CookingApplianceBlockEntity.CookingError error = blockEntity.getRecipeError();
        if (error != CookingApplianceBlockEntity.CookingError.NONE) {
            graphics.blit(TEXTURE, leftPos + ERROR_X + SLOT_SIZE + 3, topPos + ERROR_Y + 2, ERROR_U, ERROR_V, ERROR_WIDTH, ERROR_HEIGHT);
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        // Progress Tooltips
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + ERROR_X, topPos + ERROR_Y, ERROR_HOVER_WIDTH, ERROR_HOVER_HEIGHT)) {
            List<Component> progressTooltip = new ArrayList<>(List.of());

            MutableComponent openRecipes = Component.translatable("emi.view_all_recipes");
            progressTooltip.add(openRecipes);

            CookingApplianceBlockEntity.CookingError error = blockEntity.getRecipeError();
            if (error != CookingApplianceBlockEntity.CookingError.NONE) {
                progressTooltip.add(Component.empty());
                progressTooltip.add(error.getTooltipComponent());
            }

            if (blockEntity.getCookingTicks() > 0) {
                int remainingTicks = Math.max(0, blockEntity.getMaxDuration() - blockEntity.getCookingTicks());
                float remainingSeconds = (float) remainingTicks / 20.0f;

                progressTooltip.add(Component.empty());
                MutableComponent timerIcon = ((int) remainingSeconds % 2 != 0) ? Component.literal("⏳ ") : Component.literal("⌛ ");
                Component timerText = timerIcon.append(Component.translatable("tfg.tooltip.unit.seconds", String.format("%.1f", remainingSeconds)));
                progressTooltip.add(timerText);
            }

            graphics.renderComponentTooltip(font, progressTooltip, mouseX, mouseY);
        }
    }
}
