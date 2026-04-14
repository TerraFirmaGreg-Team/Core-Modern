/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */

package su.terrafirmagreg.core.client.screen;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.client.screen.TFCContainerScreen;
import net.dries007.tfc.client.screen.button.PlayerInventoryTabButton;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.compat.patchouli.PatchouliIntegration;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.SwitchInventoryTabPacket;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import su.terrafirmagreg.core.client.screen.widget.RadarGraphWidget;

public class TFGNutritionScreen extends TFCContainerScreen<Container> {
    public static final ResourceLocation TEXTURE = Helpers.identifier("textures/gui/player_nutrition.png");

    @Nullable
    private RadarGraphWidget radarGraph;

    public TFGNutritionScreen(Container container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TEXTURE);
    }

    @Override
    public void init() {
        super.init();

        // Tab buttons.
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176, 4, 20, 22, 128, 0, 1, 3, 0, 0, button -> {
            playerInventory.player.containerMenu = playerInventory.player.inventoryMenu;
            Minecraft.getInstance().setScreen(new InventoryScreen(playerInventory.player));
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new SwitchInventoryTabPacket(SwitchInventoryTabPacket.Type.INVENTORY));
        }));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176, 27, 20, 22, 128, 0, 1, 3, 32, 0, SwitchInventoryTabPacket.Type.CALENDAR));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176 - 3, 50, 20 + 3, 22, 128 + 20, 0, 1, 3, 64, 0, SwitchInventoryTabPacket.Type.NUTRITION));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176, 73, 20, 22, 128, 0, 1, 3, 96, 0, SwitchInventoryTabPacket.Type.CLIMATE));
        PatchouliIntegration.ifEnabled(() -> addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176, 96, 20, 22, 128, 0, 1, 3, 0, 32, SwitchInventoryTabPacket.Type.BOOK)));

        // Create radar graph widget.
        int graphDiameter = 100;
        int graphX = leftPos + (imageWidth - graphDiameter) / 2;
        int graphY = topPos + 15;

        radarGraph = new RadarGraphWidget(graphX, graphY, graphDiameter);

        // Configure the radar graph appearance
        radarGraph.setFillColor(0xFFFFFF00)
                .setLineColor(0xFF00DD00)
                .setLineThickness(1.0f)
                .setDrawExternalPolygon(true)
                .setExternalLineColor(0xFFAAAAAA)
                .setExternalLineThickness(0.5f)
                .setDrawCenterLines(true)
                .setCenterLineColor(0x40AAAAAA)
                .setCenterLineThickness(0.5f)
                .setStartOffset(0.2f)
                .setUseGradientFill(true)
                .setUseGradientOutline(true)
                .setCenterColor(0x00FFFFFF)
                .setGraphTooltip(() -> {
                    Player player = ClientHelpers.getPlayer();
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        float avg = data.getNutrition().getAverageNutrition();
                        return List.of(
                                Component.translatable("tfc.tooltip.nutrition"),
                                Component.translatable("tfc.tooltip.nutrition_average", String.format("%.1f%%", avg * 100)));
                    }
                    return List.of(Component.translatable("tfc.tooltip.nutrition"));
                });

        // Add variables for each nutrient.
        for (Nutrient nutrient : Nutrient.VALUES) {
            radarGraph.addVariable(createNutrientVariable(nutrient));
        }

        addRenderableWidget(radarGraph);
    }

    private RadarGraphWidget.Variable createNutrientVariable(Nutrient nutrient) {
        // Get the color from the nutrient's ChatFormatting.
        Integer colorValue = nutrient.getColor().getColor();
        int vertexColor = colorValue != null ? (0xDD000000 | (colorValue & 0x00FFFFFF)) : 0xDDFFFFFF;

        return new RadarGraphWidget.Variable(
                () -> {
                    Player player = ClientHelpers.getPlayer();
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        return data.getNutrition().getNutrient(nutrient);
                    }
                    return 0f;
                },
                0f, 1f)
                .setLabel(Helpers.translateEnum(nutrient).withStyle(nutrient.getColor()))
                .setLabelOffset(12)
                .setLabelColor(colorValue != null ? colorValue : 0x404040)
                .setVertexColor(vertexColor)
                .setTooltip(() -> {
                    Player player = ClientHelpers.getPlayer();
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        float value = data.getNutrition().getNutrient(nutrient);
                        float maxValue = 1.0f;
                        return List.of(
                                Helpers.translateEnum(nutrient).withStyle(nutrient.getColor()),
                                Component.literal(String.format("%.1f%% / %.1f%%", value * 100, maxValue * 100)));
                    }
                    return List.of(Helpers.translateEnum(nutrient).withStyle(nutrient.getColor()));
                });
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        // Render background.
        renderBg(graphics, partialTicks, mouseX, mouseY);
        // Render widgets.
        for (var widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTicks);
        }
        // Render labels.
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);
        renderLabels(graphics, mouseX, mouseY);
        graphics.pose().popPose();

        // Render radar graph tooltip.
        if (radarGraph != null) {
            radarGraph.getTooltip(mouseX, mouseY).ifPresent(tooltip -> graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY));
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
