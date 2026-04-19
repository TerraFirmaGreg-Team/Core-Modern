/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */

package su.terrafirmagreg.core.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import su.terrafirmagreg.core.client.screen.widget.PlayerListWidget;
import su.terrafirmagreg.core.client.screen.widget.RadarGraphWidget;
import su.terrafirmagreg.core.common.container.widgets.MultiToggleButton;
import su.terrafirmagreg.core.common.container.widgets.ToggleButton;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

public class TFGNutritionScreen extends TFCContainerScreen<Container> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/nutrition_screen.png");
    public static final ResourceLocation TEAM_LIST_TINT_BACKGROUND = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/team_list_tint_background.png");
    public static final ResourceLocation TEAM_LIST_TOGGLE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/team_list_player_toggle.png");
    public static final ResourceLocation HEART_1 = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/heart_1_icon.png");
    public static final ResourceLocation HEART_2 = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/heart_2_icon.png");
    public static final ResourceLocation HEART_3 = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/heart_3_icon.png");
    public static final ResourceLocation HEART_4 = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/heart_4_icon.png");

    public static final int NUTRIENT_ICON_SIZE = 12;
    public static final int HEART_ICON_SIZE = 13;
    public static final int GUI_WIDTH = 176;
    public static final int GUI_HEIGHT = 166;

    @Nullable
    private RadarGraphWidget positiveRadarGraph;
    private RadarGraphWidget negativeRadarGraph;
    private PlayerListWidget playerList;
    private ToggleButton teamToggleButton;
    private MultiToggleButton styleToggleButton;

    private final List<Float> stablePosValues = new ArrayList<>();
    private final List<Float> stableNegValues = new ArrayList<>();

    private static boolean RENDER_TEAM_NUTRITION = false;
    private static int STYLE_BUTTON_STATE = 0;

    private static final UUID[] DUMMY_UUIDS = {
            UUID.fromString("c154610e-8875-4bb5-99ef-8c167a0f2237"),
            UUID.fromString("6a85d348-cadf-4a8a-8a07-9e1a1f14ee15"),
            UUID.fromString("f66762d1-789e-467d-9171-8ff510f2e11d"),
            UUID.fromString("ffc23e0f-c6d8-4eba-b33a-cd0fccca6097"),
            UUID.fromString("cc998bd8-ea24-46b5-b2c1-b2784107c612"),
            UUID.fromString("9ca8866e-778b-46e8-b384-0af54ae3d399"),
            UUID.fromString("e213327a-7538-49fa-86ab-8c54545ca95f")
    };

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

        // Graph Values.
        int positiveGraphDiameter = 75;
        int positiveGraphX = leftPos + (GUI_WIDTH / 3) - (positiveGraphDiameter / 2);
        int positiveGraphY = topPos + (GUI_HEIGHT / 3) - (positiveGraphDiameter / 2) + (NUTRIENT_ICON_SIZE / 2);

        int negativeGraphDiameter = 45;
        int negativeGraphX = positiveGraphX + ((GUI_WIDTH / 3) * 2) - (negativeGraphDiameter / 2);
        int negativeGraphY = positiveGraphY + (positiveGraphDiameter - negativeGraphDiameter);

        int teamToggleSize = 16;
        int teamToggleX = negativeGraphX + (negativeGraphDiameter / 2) - (teamToggleSize + (teamToggleSize / 4));
        int teamToggleY = topPos + (teamToggleSize / 2);

        int styleToggleSize = 16;
        int styleToggleStates = 3;
        int styleToggleX = teamToggleX + (teamToggleSize + (teamToggleSize / 2));
        int styleToggleY = teamToggleY;

        // Create radar graph widget.
        positiveRadarGraph = new RadarGraphWidget(positiveGraphX, positiveGraphY, positiveGraphDiameter);
        negativeRadarGraph = new RadarGraphWidget(negativeGraphX, negativeGraphY, negativeGraphDiameter);

        // Heart icon.
        positiveRadarGraph.setCentralIcon(() -> {
            Player player = ClientHelpers.getPlayer();
            if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                float avg = data.getNutrition().getAverageNutrition();
                if (avg < 0.33f)
                    return HEART_1;
                if (avg < 0.66f)
                    return HEART_2;
                if (avg < 0.99f)
                    return HEART_3;
                return HEART_4;
            }
            return HEART_1;
        }, HEART_ICON_SIZE);

        // Toggle button for team nutrition.
        ResourceLocation toggleTexture = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/team_button.png");
        ToggleButton teamToggleButton = new ToggleButton(teamToggleX, teamToggleY, teamToggleSize, teamToggleSize, toggleTexture, teamToggleSize * 2, teamToggleSize, () -> RENDER_TEAM_NUTRITION,
                button -> {
                    RENDER_TEAM_NUTRITION = !RENDER_TEAM_NUTRITION;
                    updateGraphs();
                });
        addRenderableWidget(teamToggleButton);
        this.teamToggleButton = teamToggleButton;

        MultiToggleButton styleToggleButton = new MultiToggleButton(
                styleToggleX, styleToggleY, styleToggleSize, styleToggleSize,
                styleToggleStates,
                styleToggleSize, styleToggleSize,
                () -> STYLE_BUTTON_STATE,
                state -> STYLE_BUTTON_STATE = state,
                state -> ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/style_button_" + state + ".png"),
                button -> {
                    updateGraphs();
                });
        STYLE_BUTTON_STATE = styleToggleButton.getCurrentState();
        addRenderableWidget(styleToggleButton);
        this.styleToggleButton = styleToggleButton;

        // Configure the radar graph appearance
        positiveRadarGraph.setFillColor(0x9A4FE032)
                .setLineColor(0xFF35A51F)
                .setLineThickness(1.0f)
                .setDrawExternalPolygon(true)
                .setExternalLineColor(0xDD7A7A7A)
                .setExternalLineThickness(0.5f)
                .setDrawCenterLines(true)
                .setCenterLineColor(0xDD7A7A7A)
                .setCenterLineThickness(0.5f)
                .setDrawCircle(true)
                .setCircleColor(0xDDE9E9E9)
                .setCircleThickness(0.5f)
                .setStartOffset(0.2f)
                // Vertex gradient mode.
                .setUseGradientFill(false)
                .setUseGradientOutline(false)
                .setCenterColor(0x00FFFFFF)
                // Radius gradient mode.
                .setUseRadiusGradient(true)
                .setRadiusInnerColor(0xDD9e0000)
                .setRadiusMiddleColor(0xDDd1b500)
                .setRadiusOuterColor(0xDD29b000)
                .setGraphTooltip(() -> {
                    Player player = ClientHelpers.getPlayer();
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        float avg = data.getNutrition().getAverageNutrition();
                        float maxHealth = (player.getMaxHealth() * data.getHealthModifier()) / 2;
                        List<Component> components = new ArrayList<>();

                        // Title and count.
                        components.add(Component.translatable("tfg.tooltip.nutrition.positive_nutrients"));
                        if (avg < 0.3f) {
                            components.add(Component.translatable("tfg.tooltip.nutrition.positive_average",
                                    Component.literal(String.format("%.0f%%", avg * 100)).withStyle(ChatFormatting.RED)));
                            components.add(Component.translatable("tfg.tooltip.nutrition.health_modifier",
                                    Component.literal(String.format("%.1f", maxHealth)).withStyle(ChatFormatting.RED)));
                        }
                        if (avg < 0.6f && avg >= 0.3f) {
                            components.add(Component.translatable("tfg.tooltip.nutrition.positive_average",
                                    Component.literal(String.format("%.0f%%", avg * 100)).withStyle(ChatFormatting.YELLOW)));
                            components.add(Component.translatable("tfg.tooltip.nutrition.health_modifier",
                                    Component.literal(String.format("%.1f", maxHealth)).withStyle(ChatFormatting.YELLOW)));
                        }
                        if (avg < 0.9f && avg >= 0.6f) {
                            components.add(Component.translatable("tfg.tooltip.nutrition.positive_average",
                                    Component.literal(String.format("%.0f%%", avg * 100)).withStyle(ChatFormatting.GREEN)));
                            components.add(Component.translatable("tfg.tooltip.nutrition.health_modifier",
                                    Component.literal(String.format("%.1f", maxHealth)).withStyle(ChatFormatting.GREEN)));
                        }
                        if (avg >= 0.99) {
                            components.add(Component.translatable("tfg.tooltip.nutrition.positive_average", String.format("%.0f%%", avg * 100)).withStyle(ChatFormatting.GOLD));
                            components.add(Component.translatable("tfg.tooltip.nutrition.health_modifier", String.format("%.1f", maxHealth)).withStyle(ChatFormatting.GOLD));
                        }
                        components.add(Component.literal(" "));

                        // Hold Shift info.
                        if (Screen.hasShiftDown()) {
                            components.add(Component.translatable("tfg.tooltip.nutrition.positive_info").withStyle(ChatFormatting.GRAY));
                        } else {
                            components.add(Component.translatable("tfg.tooltip.shift_hint").withStyle(ChatFormatting.GOLD));
                        }
                        return components;
                    }
                    return List.of(Component.translatable("tfg.tooltip.nutrition.positive_average"));
                });

        negativeRadarGraph.setFillColor(0x9ADE2770)
                .setLineColor(0xFF8E1B49)
                .setLineThickness(1.0f)
                .setDrawExternalPolygon(true)
                .setExternalLineColor(0xDD7A7A7A)
                .setExternalLineThickness(0.5f)
                .setDrawCenterLines(true)
                .setCenterLineColor(0xDD7A7A7A)
                .setCenterLineThickness(0.5f)
                .setDrawCircle(true)
                .setCircleColor(0xDDE9E9E9)
                .setCircleThickness(0.5f)
                .setStartOffset(0.2f)
                // Vertex gradient mode.
                .setUseGradientFill(false)
                .setUseGradientOutline(false)
                .setCenterColor(0x00FFFFFF)
                // Radius gradient mode.
                .setUseRadiusGradient(true)
                .setRadiusInnerColor(0xDD29b000)
                .setRadiusMiddleColor(0xDDd1b500)
                .setRadiusOuterColor(0xDD9e0000)
                .setGraphTooltip(() -> {
                    Player player = ClientHelpers.getPlayer();
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        float negativeSum = 0;
                        for (Nutrient nutrient : Nutrient.VALUES) {
                            if (TFGNutrients.isNegative(nutrient))
                                negativeSum += data.getNutrition().getNutrient(nutrient);
                        }
                        float avg = (negativeSum / TFGNutrients.getNegativeCount());
                        List<Component> components = new ArrayList<>();

                        // Title and count.
                        components.add(Component.translatable("tfg.tooltip.nutrition.negative_nutrients"));
                        components.add(Component.translatable("tfg.tooltip.nutrition.negative_average",
                                Component.literal(String.format("%.0f%%", avg * 100)).withStyle(ChatFormatting.RED)));
                        components.add(Component.literal(" "));

                        // Hold Shift info.
                        if (Screen.hasShiftDown()) {
                            components.add(Component.translatable("tfg.tooltip.nutrition.negative_info").withStyle(ChatFormatting.GRAY));
                        } else {
                            components.add(Component.translatable("tfg.tooltip.shift_hint").withStyle(ChatFormatting.GOLD));
                        }
                        return components;
                    }
                    return List.of(Component.translatable("tfg.tooltip.nutrition.negative_average"));
                });

        // Add variables for each nutrient.
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (TFGNutrients.isPositive(nutrient)) {
                positiveRadarGraph.addVariable(createNutrientVariable(nutrient));
                stablePosValues.add((float) Math.random());
            } else if (TFGNutrients.isNegative(nutrient)) {
                negativeRadarGraph.addVariable(createNutrientVariable(nutrient));
                stableNegValues.add((float) Math.random());
            }
        }

        addRenderableWidget(positiveRadarGraph);
        addRenderableWidget(negativeRadarGraph);

        // Player list for team mode.
        int listWidth = 60;
        int listHeight = 110; // 5 players * 22 itemHeight
        playerList = new PlayerListWidget(minecraft, listWidth, listHeight, topPos + 10, topPos + 10 + listHeight, 22);
        playerList.setPlayerHeadBackground(TEAM_LIST_TINT_BACKGROUND)
                .setPlayerHeadTintProvider(RadarGraphWidget.Dataset::getLineColor)
                .setPlayerHeadBackgroundBounds(0, 0, listWidth, 16);
        playerList.setX(leftPos - listWidth - 6);
        playerList.setLeftPos(leftPos - listWidth - 6);
        playerList.setCheckboxTextureOverride(TEAM_LIST_TOGGLE, 16, 16);
        addWidget(playerList);

        updateGraphs();
    }

    private void updateGraphs() {
        if (positiveRadarGraph == null || negativeRadarGraph == null || playerList == null)
            return;

        positiveRadarGraph.clearDatasets();
        negativeRadarGraph.clearDatasets();
        playerList.clearPlayers();

        if (RENDER_TEAM_NUTRITION) {
            // Team view.
            positiveRadarGraph.setUseRadiusGradient(false);
            negativeRadarGraph.setUseRadiusGradient(false);
            positiveRadarGraph.setShowCentralIcon(false);

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
            Player self = playerInventory.player;
            RadarGraphWidget.Dataset dsPos1 = new RadarGraphWidget.Dataset(self.getName(), posValues1, 0x8000FF00, 0xFF00FF00);
            RadarGraphWidget.Dataset dsNeg1 = new RadarGraphWidget.Dataset(self.getName(), negValues1, 0x8000FF00, 0xFF00FF00);
            positiveRadarGraph.addDataset(dsPos1);
            negativeRadarGraph.addDataset(dsNeg1);
            playerList.addPlayer(self.getName(), self.getUUID(), dsPos1, dsNeg1, true);

            // Dummy Players.
            for (int i = 0; i < DUMMY_UUIDS.length; i++) {
                UUID dummyUuid = DUMMY_UUIDS[i];
                String dummyName = "Player " + (i + 2);
                var connection = Minecraft.getInstance().getConnection();
                if (connection != null) {
                    var playerInfo = connection.getPlayerInfo(dummyUuid);
                    if (playerInfo != null) {
                        dummyName = playerInfo.getProfile().getName();
                    }
                }

                boolean isMainPlayer = false;
                List<Supplier<Float>> dummyPosValues = new ArrayList<>();
                List<Supplier<Float>> dummyNegValues = new ArrayList<>();

                for (int j = 0; j < stablePosValues.size(); j++) {
                    final float base = stablePosValues.get(j);
                    final float offset = (i + 1) * (j + 1) * 0.5f;
                    dummyPosValues.add(() -> Math.max(0.1f, Math.min(0.9f, base + (float) Math.sin(offset) * 0.4f)));
                }
                for (int j = 0; j < stableNegValues.size(); j++) {
                    final float base = stableNegValues.get(j);
                    final float offset = (i + 1) * (j + 1) * 0.5f;
                    dummyNegValues.add(() -> Math.max(0.1f, Math.min(0.9f, base + (float) Math.cos(offset) * 0.4f)));
                }

                int color = 0xFF000000 | java.util.concurrent.ThreadLocalRandom.current().nextInt(0xFFFFFF);
                int fillColor = (color & 0x55FFFFFF) | 0x55000000;

                RadarGraphWidget.Dataset dsPos = new RadarGraphWidget.Dataset(Component.literal(dummyName), dummyPosValues, fillColor, color);
                RadarGraphWidget.Dataset dsNeg = new RadarGraphWidget.Dataset(Component.literal(dummyName), dummyNegValues, fillColor, color);
                positiveRadarGraph.addDataset(dsPos);
                negativeRadarGraph.addDataset(dsNeg);
                playerList.addPlayer(Component.literal(dummyName), dummyUuid, dsPos, dsNeg, false);
            }

        } else {
            // Radius Color Style Mode.
            positiveRadarGraph.setUseRadiusGradient(true);
            positiveRadarGraph.setUseGradientFill(false);
            positiveRadarGraph.setUseGradientOutline(false);
            positiveRadarGraph.setShowCentralIcon(true);

            negativeRadarGraph.setUseRadiusGradient(true);
            negativeRadarGraph.setUseGradientFill(false);
            negativeRadarGraph.setUseGradientOutline(false);

            if (STYLE_BUTTON_STATE == 1) {
                // Gradient Color Style Mode.
                positiveRadarGraph.setUseRadiusGradient(false);
                positiveRadarGraph.setUseGradientFill(true);
                positiveRadarGraph.setUseGradientOutline(true);
                positiveRadarGraph.setShowCentralIcon(true);

                negativeRadarGraph.setUseRadiusGradient(false);
                negativeRadarGraph.setUseGradientFill(true);
                negativeRadarGraph.setUseGradientOutline(true);
            }
            if (STYLE_BUTTON_STATE == 2) {
                // Solid Color Style Mode.
                positiveRadarGraph.setUseRadiusGradient(false);
                positiveRadarGraph.setUseGradientFill(false);
                positiveRadarGraph.setUseGradientOutline(false);
                positiveRadarGraph.setShowCentralIcon(true);

                negativeRadarGraph.setUseRadiusGradient(false);
                negativeRadarGraph.setUseGradientFill(false);
                negativeRadarGraph.setUseGradientOutline(false);
            }
        }
    }

    private RadarGraphWidget.Variable createNutrientVariable(Nutrient nutrient) {
        // Get the color from the nutrient's ChatFormatting.
        Integer colorValue = nutrient.getColor().getColor();
        int vertexColor = colorValue != null ? (0xDD000000 | (colorValue & 0x00FFFFFF)) : 0xDDFFFFFF;
        Player player = ClientHelpers.getPlayer();

        return new RadarGraphWidget.Variable(
                () -> {
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        return data.getNutrition().getNutrient(nutrient);
                    }
                    return 0f;
                },
                0f, 1f)
                //.setLabel(Helpers.translateEnum(nutrient).withStyle(nutrient.getColor()))
                .setTexture(() -> {
                    if (TFGNutrients.isPositive(nutrient)) {
                        if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                            float avg = data.getNutrition().getNutrient(nutrient);
                            if (avg < 0.25f)
                                return ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/" + nutrient.getSerializedName() + "_bad_icon.png");
                            if (avg < 0.99f)
                                return ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/" + nutrient.getSerializedName() + "_icon.png");
                            return ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/" + nutrient.getSerializedName() + "_good_icon.png");
                        }
                    }
                    return ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/nutrition_screen/" + nutrient.getSerializedName() + "_icon.png");
                }, NUTRIENT_ICON_SIZE)
                .setLabelOffset((NUTRIENT_ICON_SIZE / 2) + 1)
                //.setLabelColor(colorValue != null ? colorValue : 0x404040)
                .setVertexColor(vertexColor)
                .setTooltip(() -> {
                    if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                        float value = data.getNutrition().getNutrient(nutrient);
                        List<Component> components = new ArrayList<>();

                        // Title and count.
                        components.add(Helpers.translateEnum(nutrient).withStyle(nutrient.getColor()));
                        components.add(Component.literal(String.format("%.0f%%", value * 100)));
                        components.add(Component.literal(" "));

                        // Hold Shift info.
                        if (Screen.hasShiftDown()) {
                            components.add(Component.translatable("tfg.tooltip.nutrition." + nutrient.getSerializedName() + "_info").withStyle(ChatFormatting.GRAY));
                        } else {
                            components.add(Component.translatable("tfg.tooltip.shift_hint").withStyle(ChatFormatting.GOLD));
                        }

                        return components;
                    }
                    return List.of(Helpers.translateEnum(nutrient).withStyle(nutrient.getColor()));
                });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (RENDER_TEAM_NUTRITION && playerList != null) {
            if (playerList.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (RENDER_TEAM_NUTRITION && playerList != null) {
            if (playerList.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (RENDER_TEAM_NUTRITION && playerList != null) {
            if (playerList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        // Render background.
        renderBg(graphics, partialTicks, mouseX, mouseY);

        if (RENDER_TEAM_NUTRITION && playerList != null) {
            playerList.render(graphics, mouseX, mouseY, partialTicks);
        }

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
        if (positiveRadarGraph != null) {
            positiveRadarGraph.getTooltip(mouseX, mouseY).ifPresent(tooltip -> graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY));
        }
        if (negativeRadarGraph != null) {
            negativeRadarGraph.getTooltip(mouseX, mouseY).ifPresent(tooltip -> graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY));
        }

        if (teamToggleButton != null && teamToggleButton.isMouseOver(mouseX, mouseY)) {
            if (RENDER_TEAM_NUTRITION) {
                graphics.renderTooltip(this.font, Component.translatable("tfg.tooltip.nutrition.team_button.active"), mouseX, mouseY);
            } else {
                graphics.renderTooltip(this.font, Component.translatable("tfg.tooltip.nutrition.team_button.inactive"), mouseX, mouseY);
            }
        }
        if (styleToggleButton != null && styleToggleButton.isMouseOver(mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable("tfg.tooltip.nutrition.style_button_" + styleToggleButton.getCurrentState()), mouseX, mouseY);
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
