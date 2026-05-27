/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */

package su.terrafirmagreg.core.client.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.client.screen.TFCContainerScreen;
import net.dries007.tfc.client.screen.button.PlayerInventoryTabButton;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.common.capabilities.player.PlayerData;
import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.compat.patchouli.PatchouliIntegration;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.SwitchInventoryTabPacket;
import net.dries007.tfc.util.Helpers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.screen.widget.MultiToggleButton;
import su.terrafirmagreg.core.client.screen.widget.PlayerListWidget;
import su.terrafirmagreg.core.client.screen.widget.RadarGraphWidget;
import su.terrafirmagreg.core.client.screen.widget.ToggleButton;
import su.terrafirmagreg.core.client.screen.widget.ValueDisplayListWidget;
import su.terrafirmagreg.core.client.util.TFGTooltipUtils;
import su.terrafirmagreg.core.common.food.nutrient.NutrientEffectsHandler;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.RequestTeamNutritionPacket;

/**
 * TFG Nutrition Screen that overrides the default TFC Nutrition Screen.
 */
@SuppressWarnings({ "UnnecessaryLocalVariable" })
public class TFGNutritionScreen extends TFCContainerScreen<Container> {
    public static final ResourceLocation TEXTURE = TFGCore.id("textures/gui/nutrition_screen/nutrition_screen.png");
    public static final ResourceLocation TEAM_LIST_TINT_BACKGROUND = TFGCore.id("textures/gui/nutrition_screen/team_list_tint_background.png");
    public static final ResourceLocation TEAM_LIST_TOGGLE = TFGCore.id("textures/gui/nutrition_screen/team_list_player_toggle.png");
    public static final ResourceLocation HEART_1 = TFGCore.id("textures/gui/nutrition_screen/heart_1_icon.png");
    public static final ResourceLocation HEART_2 = TFGCore.id("textures/gui/nutrition_screen/heart_2_icon.png");
    public static final ResourceLocation HEART_3 = TFGCore.id("textures/gui/nutrition_screen/heart_3_icon.png");
    public static final ResourceLocation HEART_4 = TFGCore.id("textures/gui/nutrition_screen/heart_4_icon.png");
    public static final ResourceLocation SCROLLBAR_BACKGROUND = TFGCore.id("textures/gui/nutrition_screen/scrollbar_background.png");
    public static final ResourceLocation SCROLLBAR_GRABBER = TFGCore.id("textures/gui/nutrition_screen/scrollbar_grabber.png");

    public static final int NUTRIENT_ICON_SIZE = 12;
    public static final int HEART_ICON_SIZE = 13;
    public static final int GUI_WIDTH = 176;
    public static final int GUI_HEIGHT = 188;
    private static boolean RENDER_TEAM_NUTRITION = false;
    private static int STYLE_BUTTON_STATE = 0;
    private static final Map<UUID, float[]> CACHED_TEAM_NUTRITION = new HashMap<>();

    public static void receiveTeamNutrition(Map<UUID, float[]> data) {
        CACHED_TEAM_NUTRITION.putAll(data);
    }

    /**
     * Enables development mode for team list features.
     * When enabled, dummy team players are added to the radar graphs at {@link #addDummyTeamPlayers()}.
     * And {@link #addFtbTeamPlayers(UUID)} is disabled.
     */
    private static final boolean NUTRITION_TEAM_DEV_MODE = false;

    @Nullable
    private RadarGraphWidget positiveRadarGraph;
    private RadarGraphWidget negativeRadarGraph;
    private PlayerListWidget playerList;
    private ValueDisplayListWidget valueDisplayList;
    private ToggleButton teamToggleButton;
    private MultiToggleButton styleToggleButton;

    private final List<Float> stablePosValues = new ArrayList<>();
    private final List<Float> stableNegValues = new ArrayList<>();

    /**
     * Dummy UUIDs for testing the team list when {@link #NUTRITION_TEAM_DEV_MODE} is enabled.
     */
    private static final UUID[] DUMMY_UUIDS = {
            UUID.fromString("c154610e-8875-4bb5-99ef-8c167a0f2237"), // Pyritie
            UUID.fromString("6a85d348-cadf-4a8a-8a07-9e1a1f14ee15"), // Mqrius
            UUID.fromString("f66762d1-789e-467d-9171-8ff510f2e11d"), // SeuSherbert
            UUID.fromString("ffc23e0f-c6d8-4eba-b33a-cd0fccca6097"), // Flurben
            UUID.fromString("cc998bd8-ea24-46b5-b2c1-b2784107c612"), // Sakura
            UUID.fromString("9ca8866e-778b-46e8-b384-0af54ae3d399"), // Totor1
            UUID.fromString("e213327a-7538-49fa-86ab-8c54545ca95f"), // Broofsi
            UUID.fromString("a54aeee0-fc04-41a6-8ae8-5b7eab34c16c"), // Arke
            UUID.fromString("57b3dfb5-f8a6-49e2-8b54-4e4ffc63256f"), // Xikaro
    };

    public TFGNutritionScreen(Container container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TEXTURE);
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void init() {
        super.init();

        Player player = ClientHelpers.getPlayer();
        assert player != null;
        PlayerData playerData = PlayerData.get(player);
        TFCFoodData foodData = (TFCFoodData) player.getFoodData();

        // ---- Screen Values. ----

        float thirstModifier = foodData.getThirstModifier(player);
        float saturation = foodData.getSaturationLevel();
        long intoxication = (playerData.getIntoxicatedTicks() / 20) / 60;
        float passiveExhaustion = TFCFoodData.PASSIVE_EXHAUSTION_PER_SECOND * NutrientEffectsHandler.getParasitesPassiveExhaustionModifier(player.getUUID());
        float exhaustionMultiplier = TFCFoodData.EXHAUSTION_MULTIPLIER * NutrientEffectsHandler.getProteinExhaustionMultiplier(player.getUUID());
        float passiveHealing = TFCFoodData.PASSIVE_HEALING_PER_TEN_TICKS * 2 * 100 * 3 * NutrientEffectsHandler.getHealingModifierMultiplier(player.getUUID());

        int positiveGraphDiameter = 75;
        int positiveGraphX = leftPos + (GUI_WIDTH / 3) - (positiveGraphDiameter / 2);
        int positiveGraphY = topPos + (GUI_HEIGHT / 3) - (positiveGraphDiameter / 2);

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

        int variableDisplayOffset = 2;
        int variableDisplayX = leftPos + (variableDisplayOffset * 4);
        int variableDisplayY = positiveGraphY + positiveGraphDiameter + 17;
        int variableDisplayWidth = GUI_WIDTH - 22;
        int valueDisplayRowHeight = this.font.lineHeight + 5;
        int valueDisplayRows = 4;
        int variableDisplayHeight = (valueDisplayRowHeight * valueDisplayRows) + variableDisplayOffset;

        int titleLabelX = leftPos + 8;
        int titleLabelY = topPos + 6;
        int titleLabelWidth = positiveGraphDiameter;
        int titleLabelHeight = 9;

        int listItemSize = 16;
        int listWidth = 60;
        int listHeight = GUI_HEIGHT;
        int listX = leftPos - listWidth - 7;

        // ---- Tab Buttons. ----

        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176, 4, 20, 22, 128, 0, 1, 3, 0, 0, button -> {
            playerInventory.player.containerMenu = playerInventory.player.inventoryMenu;
            Minecraft.getInstance().setScreen(new InventoryScreen(playerInventory.player));
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new SwitchInventoryTabPacket(SwitchInventoryTabPacket.Type.INVENTORY));
        }));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176, 27, 20, 22, 128, 0, 1, 3, 32, 0, SwitchInventoryTabPacket.Type.CALENDAR));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176 - 3, 50, 20 + 3, 22, 128 + 20, 0, 1, 3, 64, 0, SwitchInventoryTabPacket.Type.NUTRITION));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176, 73, 20, 22, 128, 0, 1, 3, 96, 0, SwitchInventoryTabPacket.Type.CLIMATE));
        PatchouliIntegration.ifEnabled(() -> addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, 176, 96, 20, 22, 128, 0, 1, 3, 0, 32, SwitchInventoryTabPacket.Type.BOOK)));

        // ---- Title Display. ----

        StringWidget titleLabel = new StringWidget(titleLabelX, titleLabelY, titleLabelWidth, titleLabelHeight, Component.translatable("tfg.tooltip.nutrition.health"), this.font) {
            @Override
            public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                graphics.drawString(TFGNutritionScreen.this.font, this.getMessage(), this.getX(), this.getY() + (this.height - 9) / 2, 0x000000, false);
            }
        };
        titleLabel.alignLeft();
        this.addRenderableWidget(titleLabel);

        // ---- Value Displays. ----

        valueDisplayList = new ValueDisplayListWidget(minecraft, this.font, variableDisplayWidth, variableDisplayHeight, variableDisplayY, variableDisplayY + variableDisplayHeight,
                valueDisplayRowHeight);
        valueDisplayList.setScrollbarBackgroundTexture(SCROLLBAR_BACKGROUND, 6);
        valueDisplayList.setScrollbarGrabberTexture(SCROLLBAR_GRABBER, 6);
        valueDisplayList.setX(variableDisplayX);
        valueDisplayList.setLeftPos(variableDisplayX);

        // Thirst Modifier Display.
        valueDisplayList.addValue(
                Component.translatable("tfg.tooltip.nutrition.thirst_modifier_display", Component.literal(String.format("%.1f", thirstModifier)).withStyle(ChatFormatting.AQUA)),
                Component.translatable("tfg.tooltip.nutrition.thirst_modifier_info"));

        // Saturation Level Display.
        valueDisplayList.addValue(
                Component.translatable("tfg.tooltip.nutrition.saturation_display", Component.literal(String.format("%.0f", saturation)).withStyle(ChatFormatting.YELLOW)),
                Component.translatable("tfg.tooltip.nutrition.saturation_info"));

        // Intoxication Display.
        valueDisplayList.addValue(
                Component.translatable("tfg.tooltip.nutrition.intoxication_display", Component.literal(String.format("%d", intoxication)).withStyle(ChatFormatting.LIGHT_PURPLE)),
                Component.translatable("tfg.tooltip.nutrition.intoxication_info"));

        // Passive Healing Display.
        valueDisplayList.addValue(
                Component.translatable("tfg.tooltip.nutrition.passive_healing_display", Component.literal(String.format("%.3f", passiveHealing / 100)).withStyle(ChatFormatting.RED)),
                Component.translatable("tfg.tooltip.nutrition.passive_healing_info", Component.literal(String.format("%.1f", passiveHealing))));

        // Exhaustion Display.
        valueDisplayList.addValue(
                Component.translatable("tfg.tooltip.nutrition.exhaustion_display", Component.literal(String.format("%.1f", passiveExhaustion)).withStyle(ChatFormatting.GREEN)),
                Component.translatable("tfg.tooltip.nutrition.exhaustion_info"));

        // Exhaustion Multiplier.
        valueDisplayList.addValue(
                Component.translatable("tfg.tooltip.nutrition.exhaustion_multiplier_display", Component.literal(String.format("%.1f", exhaustionMultiplier)).withStyle(ChatFormatting.DARK_GREEN)),
                Component.translatable("tfg.tooltip.nutrition.exhaustion_multiplier_info"));

        addWidget(valueDisplayList);

        // ---- Radar Graphs ----

        // Create radar graph widget.
        positiveRadarGraph = new RadarGraphWidget(positiveGraphX, positiveGraphY, positiveGraphDiameter);
        negativeRadarGraph = new RadarGraphWidget(negativeGraphX, negativeGraphY, negativeGraphDiameter);

        // Heart icon.
        positiveRadarGraph.setCentralIcon(() -> {
            if (player.getFoodData() instanceof TFCFoodData data) {
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
        ResourceLocation toggleTexture = TFGCore.id("textures/gui/nutrition_screen/team_button.png");
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
                state -> TFGCore.id("textures/gui/nutrition_screen/style_button_" + state + ".png"),
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
                    if (player.getFoodData() instanceof TFCFoodData data) {
                        List<Component> components = new ArrayList<>();
                        float avg = data.getNutrition().getAverageNutrition();
                        float maxHealth = (player.getMaxHealth() * data.getHealthModifier()) / 2;

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
                        if (avg < 0.95f && avg >= 0.6f) {
                            components.add(Component.translatable("tfg.tooltip.nutrition.positive_average",
                                    Component.literal(String.format("%.0f%%", avg * 100)).withStyle(ChatFormatting.GREEN)));
                            components.add(Component.translatable("tfg.tooltip.nutrition.health_modifier",
                                    Component.literal(String.format("%.1f", maxHealth)).withStyle(ChatFormatting.GREEN)));
                        }
                        if (avg >= 0.95f) {
                            components.add(Component.translatable("tfg.tooltip.nutrition.positive_average", String.format("%.0f%%", avg * 100)).withStyle(ChatFormatting.GOLD));
                            components.add(Component.translatable("tfg.tooltip.nutrition.health_modifier", String.format("%.1f", maxHealth)).withStyle(ChatFormatting.GOLD));
                        }
                        components.add(Component.literal(" "));

                        // Hold Shift info.
                        if (Screen.hasShiftDown()) {
                            components.addAll(TFGTooltipUtils.normalize(Component.translatable("tfg.tooltip.nutrition.positive_info").withStyle(ChatFormatting.GRAY)));
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
                    if (player.getFoodData() instanceof TFCFoodData data) {
                        List<Component> components = new ArrayList<>();
                        float negativeSum = 0;
                        for (Nutrient nutrient : Nutrient.VALUES) {
                            if (TFGNutrients.isNegative(nutrient))
                                negativeSum += data.getNutrition().getNutrient(nutrient);
                        }
                        float avg = (negativeSum / TFGNutrients.getNegativeCount());

                        // Title and count.
                        components.add(Component.translatable("tfg.tooltip.nutrition.negative_nutrients"));
                        components.add(Component.translatable("tfg.tooltip.nutrition.negative_average",
                                Component.literal(String.format("%.0f%%", avg * 100)).withStyle(ChatFormatting.RED)));
                        components.add(Component.literal(" "));

                        // Hold Shift info.
                        if (Screen.hasShiftDown()) {
                            components.addAll(TFGTooltipUtils.normalize(Component.translatable("tfg.tooltip.nutrition.negative_info").withStyle(ChatFormatting.GRAY)));
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

        // ---- Player List. ----

        playerList = new PlayerListWidget(minecraft, listWidth, listHeight, topPos, topPos + listHeight, listItemSize + 4);
        playerList.setScrollbarBackgroundTexture(SCROLLBAR_BACKGROUND, 6);
        playerList.setScrollbarGrabberTexture(SCROLLBAR_GRABBER, 6);
        playerList.setPlayerHeadBackground(TEAM_LIST_TINT_BACKGROUND)
                .setPlayerHeadTintProvider(RadarGraphWidget.Dataset::getLineColor)
                .setPlayerHeadBackgroundBounds(0, 0, listWidth, listItemSize);
        playerList.setX(listX);
        playerList.setLeftPos(listX);
        playerList.setCheckboxTextureOverride(TEAM_LIST_TOGGLE, listItemSize, listItemSize);
        addWidget(playerList);

        updateGraphs();
    }

    /**
     * Updates the radar graphs and player list with current nutrition data.
     */
    private void updateGraphs() {
        if (positiveRadarGraph == null || negativeRadarGraph == null || playerList == null) {
            return;
        }

        positiveRadarGraph.clearDatasets();
        negativeRadarGraph.clearDatasets();
        playerList.clearPlayers();

        if (RENDER_TEAM_NUTRITION) {
            // Team view.
            positiveRadarGraph.setUseRadiusGradient(false);
            negativeRadarGraph.setUseRadiusGradient(false);
            positiveRadarGraph.setShowCentralIcon(false);

            TFGNetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestTeamNutritionPacket());

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
            playerList.addPlayer(self.getName(), self.getUUID(), dsPos1, dsNeg1, true, true);

            if (NUTRITION_TEAM_DEV_MODE) {
                addDummyTeamPlayers();
            } else {
                addFtbTeamPlayers(self.getUUID());
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

    /**
     * Adds dummy team players to the radar graphs for testing purposes.
     * Only works if {@link #NUTRITION_TEAM_DEV_MODE} is enabled.
     */
    private void addDummyTeamPlayers() {
        if (positiveRadarGraph == null || negativeRadarGraph == null || playerList == null) {
            return;
        }
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

            int color = getDistributedTeamColor(i, DUMMY_UUIDS.length);
            int fillColor = (color & 0x55FFFFFF) | 0x55000000;

            RadarGraphWidget.Dataset dsPos = new RadarGraphWidget.Dataset(Component.literal(dummyName), dummyPosValues, fillColor, color);
            RadarGraphWidget.Dataset dsNeg = new RadarGraphWidget.Dataset(Component.literal(dummyName), dummyNegValues, fillColor, color);
            positiveRadarGraph.addDataset(dsPos);
            negativeRadarGraph.addDataset(dsNeg);
            playerList.addPlayer(Component.literal(dummyName), dummyUuid, dsPos, dsNeg, false, i % 2 == 0);
        }
    }

    /**
     * Adds team players from FTB Teams.
     */
    private void addFtbTeamPlayers(UUID selfUuid) {
        if (positiveRadarGraph == null || negativeRadarGraph == null || playerList == null) {
            return;
        }
        List<UUID> teamMembers = new ArrayList<>();
        if (ModList.get().isLoaded("ftbteams")) {
            teamMembers = resolveFtbTeamMembers(selfUuid);
        }
        if (teamMembers.isEmpty()) {
            return;
        }

        int memberCount = Math.max(1, teamMembers.size() - 1);
        int memberIndex = 0;
        for (UUID memberUuid : teamMembers) {
            if (memberUuid.equals(selfUuid)) {
                continue;
            }

            String memberName = "Player";
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                var playerInfo = connection.getPlayerInfo(memberUuid);
                if (playerInfo != null) {
                    memberName = playerInfo.getProfile().getName();
                }
            }

            List<Supplier<Float>> posValues = new ArrayList<>();
            List<Supplier<Float>> negValues = new ArrayList<>();

            for (Nutrient nutrient : Nutrient.VALUES) {
                Supplier<Float> supplier = () -> {
                    var level = Minecraft.getInstance().level;
                    if (level == null) {
                        return 0f;
                    }
                    float[] cached = CACHED_TEAM_NUTRITION.get(memberUuid);
                    if (cached != null && nutrient.ordinal() < cached.length) {
                        return cached[nutrient.ordinal()];
                    }
                    Player member = level.getPlayerByUUID(memberUuid);
                    if (member != null && member.getFoodData() instanceof TFCFoodData data) {
                        return data.getNutrition().getNutrient(nutrient);
                    }
                    return 0f;
                };
                if (TFGNutrients.isPositive(nutrient)) {
                    posValues.add(supplier);
                } else if (TFGNutrients.isNegative(nutrient)) {
                    negValues.add(supplier);
                }
            }

            int color = getDistributedTeamColor(memberIndex, memberCount);
            int fillColor = (color & 0x55FFFFFF) | 0x55000000;

            RadarGraphWidget.Dataset dsPos = new RadarGraphWidget.Dataset(Component.literal(memberName), posValues, fillColor, color);
            RadarGraphWidget.Dataset dsNeg = new RadarGraphWidget.Dataset(Component.literal(memberName), negValues, fillColor, color);
            positiveRadarGraph.addDataset(dsPos);
            negativeRadarGraph.addDataset(dsNeg);

            boolean memberOnline = connection != null && connection.getPlayerInfo(memberUuid) != null;
            playerList.addPlayer(Component.literal(memberName), memberUuid, dsPos, dsNeg, false, memberOnline);
            memberIndex++;
        }
    }

    /**
     * Calculates a distributed color for team members based on their index and total count.
     * @param index The index of the team member.
     * @param totalPlayers The total number of team members.
     * @return The ARGB color value for the team member.
     */
    private int getDistributedTeamColor(int index, int totalPlayers) {
        int count = Math.max(1, totalPlayers);
        float hueStep = 1.0f / count;
        float hueOffset = 0.08f;
        float hue = (hueOffset + (index * hueStep)) % 1.0f;

        // Keep colors vivid
        // Saturation > 50%, lightness in ~30-60% range.
        float saturation = 0.65f;
        float lightness = 0.45f;
        if ((index & 1) == 1) {
            lightness = 0.55f;
        }
        if (index % 3 == 2) {
            saturation = 0.75f;
        }

        return hslToArgb(hue, saturation, lightness);
    }

    /**
     * Converts HSL color model to ARGB color model.
     * @param hue The hue [0, 1].
     * @param saturation The saturation [0, 1].
     * @param lightness The lightness [0, 1].
     * @return The ARGB color value.
     */
    private int hslToArgb(float hue, float saturation, float lightness) {
        float chroma = (1.0f - Math.abs((2.0f * lightness) - 1.0f)) * saturation;
        float huePrime = (hue * 6.0f) % 6.0f;
        float x = chroma * (1.0f - Math.abs((huePrime % 2.0f) - 1.0f));

        float red1 = 0.0f;
        float green1 = 0.0f;
        float blue1 = 0.0f;

        if (huePrime < 1.0f) {
            red1 = chroma;
            green1 = x;
        } else if (huePrime < 2.0f) {
            red1 = x;
            green1 = chroma;
        } else if (huePrime < 3.0f) {
            green1 = chroma;
            blue1 = x;
        } else if (huePrime < 4.0f) {
            green1 = x;
            blue1 = chroma;
        } else if (huePrime < 5.0f) {
            red1 = x;
            blue1 = chroma;
        } else {
            red1 = chroma;
            blue1 = x;
        }

        float match = lightness - (chroma / 2.0f);
        int red = Math.round((red1 + match) * 255.0f);
        int green = Math.round((green1 + match) * 255.0f);
        int blue = Math.round((blue1 + match) * 255.0f);

        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    /**
     * Gets FTB team members for a given player UUID.
     * @param selfUuid The UUID of the player.
     * @return A list of UUIDs representing team members, including the player themselves.
     */
    private List<UUID> resolveFtbTeamMembers(UUID selfUuid) {
        if (!ModList.get().isLoaded("ftbteams")) {
            return List.of(selfUuid);
        }
        try {
            var api = FTBTeamsAPI.api();
            var manager = api.getClientManager();
            var team = manager.selfTeam();

            if (team == null) {
                return List.of(selfUuid);
            }

            List<UUID> members = new ArrayList<>(team.getMembers());
            if (members.isEmpty()) {
                return List.of(selfUuid);
            }
            members = new ArrayList<>(new java.util.LinkedHashSet<>(members));
            if (!members.contains(selfUuid)) {
                members.add(0, selfUuid);
            }
            return members;
        } catch (Exception e) {
            TFGCore.LOGGER.error("Failed to resolve FTB team members", e);
            return List.of(selfUuid);
        }
    }

    /**
     * Creates a radar graph variable for a nutrient.
     * @param nutrient The nutrient to create the variable for.
     * @return The radar graph variable.
     */
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
                .setTexture(() -> {
                    if (TFGNutrients.isPositive(nutrient)) {
                        if (player != null && player.getFoodData() instanceof TFCFoodData data) {
                            float avg = data.getNutrition().getNutrient(nutrient);
                            if (avg < 0.25f)
                                return TFGCore.id("textures/gui/nutrition_screen/" + nutrient.getSerializedName() + "_bad_icon.png");
                            if (avg < 0.99f)
                                return TFGCore.id("textures/gui/nutrition_screen/" + nutrient.getSerializedName() + "_icon.png");
                            return TFGCore.id("textures/gui/nutrition_screen/" + nutrient.getSerializedName() + "_good_icon.png");
                        }
                    }
                    return TFGCore.id("textures/gui/nutrition_screen/" + nutrient.getSerializedName() + "_icon.png");
                }, NUTRIENT_ICON_SIZE)
                .setLabelOffset((NUTRIENT_ICON_SIZE / 2) + 1)
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
                            components.addAll(TFGTooltipUtils.normalize(Component.translatable("tfg.tooltip.nutrition." + nutrient.getSerializedName() + "_info").withStyle(ChatFormatting.GRAY)));
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
        if (valueDisplayList != null && valueDisplayList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (RENDER_TEAM_NUTRITION && playerList != null) {
            if (playerList.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (valueDisplayList != null && valueDisplayList.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        if (RENDER_TEAM_NUTRITION && playerList != null) {
            if (playerList.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (valueDisplayList != null && valueDisplayList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        if (RENDER_TEAM_NUTRITION && playerList != null) {
            if (playerList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        if (valueDisplayList != null) {
            valueDisplayList.render(graphics, mouseX, mouseY, partialTicks);
        }

        if (RENDER_TEAM_NUTRITION && playerList != null) {
            playerList.render(graphics, mouseX, mouseY, partialTicks);
        }

        // Render tooltips last.
        if (valueDisplayList != null) {
            valueDisplayList.renderTooltip(graphics);
        }

        if (RENDER_TEAM_NUTRITION && playerList != null) {
            playerList.renderTooltip(graphics);
        }

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
