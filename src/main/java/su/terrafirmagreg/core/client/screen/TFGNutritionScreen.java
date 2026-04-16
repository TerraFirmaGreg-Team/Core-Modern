/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */

package su.terrafirmagreg.core.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.screen.widget.RadarGraphWidget;
import su.terrafirmagreg.core.common.container.widgets.ToggleButton;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

public class TFGNutritionScreen extends TFCContainerScreen<Container> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/nutrition_screen.png");
    public static final int ICON_SIZE = 10;

    @Nullable
    private RadarGraphWidget postiveRadarGraph;
    private RadarGraphWidget negativeRadarGraph;

    private boolean showTeamNutrition = false;
    private final List<Float> stablePosValues = new ArrayList<>();
    private final List<Float> stableNegValues = new ArrayList<>();

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
        int graphDiameter = 50;
        int positiveGraphX = leftPos + (graphDiameter / 4) + (ICON_SIZE);
        int negativeGraphX = positiveGraphX + ((graphDiameter / 2) * 3);
        int graphY = topPos + (graphDiameter / 4) + (ICON_SIZE);

        postiveRadarGraph = new RadarGraphWidget(positiveGraphX, graphY, graphDiameter);
        negativeRadarGraph = new RadarGraphWidget(negativeGraphX, graphY, graphDiameter);

        // Toggle button for team nutrition.
        ResourceLocation toggleTexture = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/toggle_button.png");
        addRenderableWidget(new ToggleButton(leftPos + 5, topPos + 5, 20, 20, toggleTexture, 40, 20, () -> showTeamNutrition, button -> {
            showTeamNutrition = !showTeamNutrition;
            updateGraphs();
        }));

        // Configure the radar graph appearance
        postiveRadarGraph.setFillColor(0xFFFFFF00)
                .setLineColor(0xFF00DD00)
                .setLineThickness(1.0f)
                .setDrawExternalPolygon(true)
                .setExternalLineColor(0xDD181818)
                .setExternalLineThickness(0.5f)
                .setDrawCenterLines(true)
                .setCenterLineColor(0xDD181818)
                .setCenterLineThickness(0.5f)
                .setDrawCircle(true)
                .setCircleColor(0xDD7A7A7A)
                .setCircleThickness(0.5f)
                .setStartOffset(0.2f)
                // Vertex gradient mode.
                // .setUseGradientFill(true)
                // .setUseGradientOutline(true)
                // .setCenterColor(0x00FFFFFF)
                // Radius gradient mode.
                .setUseRadiusGradient(true)
                .setRadiusInnerColor(0xDD9e0000)
                .setRadiusMiddleColor(0xDDd1b500)
                .setRadiusOuterColor(0xDD29b000)
                .setGraphTooltip(() -> {
                    Player player = ClientHelpers.getPlayer();
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        float avg = data.getNutrition().getAverageNutrition();
                        return List.of(
                                Component.translatable("tfc.tooltip.nutrition"),
                                Component.translatable("tfc.tooltip.nutrition.positive_average", String.format("%.1f%%", avg * 100)));
                    }
                    return List.of(Component.translatable("tfc.tooltip.nutrition"));
                });

        negativeRadarGraph.setFillColor(0xFFFFFF00)
                .setLineColor(0xFF00DD00)
                .setLineThickness(1.0f)
                .setDrawExternalPolygon(true)
                .setExternalLineColor(0xDD181818)
                .setExternalLineThickness(0.5f)
                .setDrawCenterLines(true)
                .setCenterLineColor(0xDD181818)
                .setCenterLineThickness(0.5f)
                .setDrawCircle(true)
                .setCircleColor(0xDD7A7A7A)
                .setCircleThickness(0.5f)
                .setStartOffset(0.2f)
                // Vertex gradient mode.
                // .setUseGradientFill(true)
                // .setUseGradientOutline(true)
                // .setCenterColor(0x00FFFFFF)
                // Radius gradient mode.
                .setUseRadiusGradient(true)
                .setRadiusInnerColor(0xDD29b000)
                .setRadiusMiddleColor(0xDDd1b500)
                .setRadiusOuterColor(0xDD9e0000)
                .setGraphTooltip(() -> {
                    Player player = ClientHelpers.getPlayer();
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        float avg = data.getNutrition().getAverageNutrition();
                        return List.of(
                                Component.translatable("tfc.tooltip.nutrition"),
                                Component.translatable("tfc.tooltip.nutrition.negative_average", String.format("%.1f%%", avg * 100)));
                    }
                    return List.of(Component.translatable("tfc.tooltip.nutrition"));
                });

        // Add variables for each nutrient.
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (TFGNutrients.isPositive(nutrient)) {
                postiveRadarGraph.addVariable(createNutrientVariable(nutrient));
                stablePosValues.add((float) Math.random());
            } else if (TFGNutrients.isNegative(nutrient)) {
                negativeRadarGraph.addVariable(createNutrientVariable(nutrient));
                stableNegValues.add((float) Math.random());
            }
        }

        addRenderableWidget(postiveRadarGraph);
        addRenderableWidget(negativeRadarGraph);

        updateGraphs();
    }

    private void updateGraphs() {
        if (postiveRadarGraph == null || negativeRadarGraph == null)
            return;

        postiveRadarGraph.clearDatasets();
        negativeRadarGraph.clearDatasets();

        if (showTeamNutrition) {
            // Team view.
            postiveRadarGraph.setUseRadiusGradient(false);
            negativeRadarGraph.setUseRadiusGradient(false);

            // Current Player
            List<Supplier<Float>> posValues1 = new ArrayList<>();
            List<Supplier<Float>> negValues1 = new ArrayList<>();
            for (Nutrient nutrient : Nutrient.VALUES) {
                Supplier<Float> supplier = () -> {
                    Player player = ClientHelpers.getPlayer();
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        return data.getNutrition().getNutrient(nutrient);
                    }
                    return 0f;
                };
                if (TFGNutrients.isPositive(nutrient))
                    posValues1.add(supplier);
                else if (TFGNutrients.isNegative(nutrient))
                    negValues1.add(supplier);
            }
            postiveRadarGraph.addDataset(new RadarGraphWidget.Dataset(Component.literal("Player 1"), posValues1, 0x8000FF00, 0xFF00FF00));
            negativeRadarGraph.addDataset(new RadarGraphWidget.Dataset(Component.literal("Player 1"), negValues1, 0x8000FF00, 0xFF00FF00));

            // "Second Player"
            List<Supplier<Float>> posValues2 = new ArrayList<>();
            List<Supplier<Float>> negValues2 = new ArrayList<>();
            for (int i = 0; i < stablePosValues.size(); i++) {
                final int index = i;
                posValues2.add(() -> stablePosValues.get(index));
            }
            for (int i = 0; i < stableNegValues.size(); i++) {
                final int index = i;
                negValues2.add(() -> stableNegValues.get(index));
            }
            postiveRadarGraph.addDataset(new RadarGraphWidget.Dataset(Component.literal("Player 2"), posValues2, 0x80FF0000, 0xFFFF0000));
            negativeRadarGraph.addDataset(new RadarGraphWidget.Dataset(Component.literal("Player 2"), negValues2, 0x80FF0000, 0xFFFF0000));

        } else {
            // Individual view: Radius gradient enabled.
            postiveRadarGraph.setUseRadiusGradient(true);
            negativeRadarGraph.setUseRadiusGradient(true);
        }
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
                //.setLabel(Helpers.translateEnum(nutrient).withStyle(nutrient.getColor()))
                .setTexture(ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/" + nutrient.getSerializedName() + "_icon.png"), ICON_SIZE)
                .setLabelOffset((ICON_SIZE / 2) + 1)
                //.setLabelColor(colorValue != null ? colorValue : 0x404040)
                .setVertexColor(vertexColor)
                .setTooltip(() -> {
                    Player player = ClientHelpers.getPlayer();
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        float value = data.getNutrition().getNutrient(nutrient);
                        float maxValue = 1.0f;
                        List<Component> components = new ArrayList<>();

                        // Title and count.
                        components.add(Helpers.translateEnum(nutrient).withStyle(nutrient.getColor()));
                        components.add(Component.literal(String.format("%.0f / %.0f%%", value * 100, maxValue * 100)));
                        components.add(Component.literal(" "));

                        // Hold Shift info.
                        if (Screen.hasShiftDown()) {
                            components.add(Component.translatable("tooltip.tfg.nutrition." + nutrient.getSerializedName() + "_info").withStyle(ChatFormatting.GRAY));
                        } else {
                            components.add(Component.translatable("tfg.tooltip.shift_hint").withStyle(ChatFormatting.GOLD));
                        }

                        return components;
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
        if (postiveRadarGraph != null) {
            postiveRadarGraph.getTooltip(mouseX, mouseY).ifPresent(tooltip -> graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY));
        }
        if (negativeRadarGraph != null) {
            negativeRadarGraph.getTooltip(mouseX, mouseY).ifPresent(tooltip -> graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY));
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
