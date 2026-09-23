/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen.button;

import net.dries007.tfc.client.ClimateRenderCache;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.TemperatureDisplayStyle;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.SwitchInventoryTabPacket;

import java.util.List;
import java.util.Objects;

public class PlayerInventoryTabButton extends Button
{
    public enum Tab
    {
        INVENTORY(0, 0, 176, 4),
        CALENDAR(16, 0, 176, 27),
        NUTRITION(32, 0, 176, 50),
        CLIMATE(48, 0, 176, 73),
        BOOK(64, 0, 176, 96);

        Tab(int iconU, int iconV, int xIn, int yIn)
        {
            this.iconU = iconU;
            this.iconV = iconV;
            this.xIn = xIn;
            this.yIn = yIn;
        }

        public final int iconU;
        public final int iconV;
        public final int xIn;
        public final int yIn;

        public static final PlayerInventoryTabButton.Tab[] VALUES = values();
    }

    private final int textureU;
    private final int textureV;
    private int iconX;
    private int iconY;
    private int prevGuiLeft;
    private int prevGuiTop;
    private final Tab tab;
    private Runnable tickCallback;
    private final boolean active;
    private final boolean detached;

    public PlayerInventoryTabButton(int guiLeft, int guiTop, boolean active, boolean detached, Tab tab)
    {
        this(guiLeft, guiTop, active, detached, tab, button ->
                PacketHandler.send(net.minecraftforge.network.PacketDistributor.SERVER.noArg(), new SwitchInventoryTabPacket(tab))
        );
    }

    public PlayerInventoryTabButton(int guiLeft, int guiTop, boolean active, boolean detached, Tab tab, OnPress onPressIn)
    {
        super(
                detached ? (guiLeft + tab.xIn + 110) : (guiLeft + tab.xIn + (active ? -3 : -2)),
                detached ? (guiTop + tab.yIn + 5) : (guiTop + tab.yIn),
                24, 22,
                Component.empty(),
                onPressIn,
                DEFAULT_NARRATION
        );
        this.prevGuiLeft = guiLeft;
        this.prevGuiTop = guiTop;
        this.textureU = detached ? (active ? 72 : 48) : (active ? 24 : 0);
        this.textureV = 16;
        this.iconX = detached ? (guiLeft + tab.xIn + 113 + 1) : (guiLeft + tab.xIn + 1);
        this.iconY = detached ? (guiTop + tab.yIn + 4 + 4) : (guiTop + tab.yIn + 3);
        this.tickCallback = () -> {};
        this.tab = tab;
        this.active = active;
        this.detached = detached;
    }

    public PlayerInventoryTabButton setRecipeBookCallback(InventoryScreen screen)
    {
        this.tickCallback = new Runnable()
        {
            boolean recipeBookVisible = screen.getRecipeBookComponent().isVisible();

            @Override
            public void run()
            {
                boolean newRecipeBookVisible = screen.getRecipeBookComponent().isVisible();
                if (newRecipeBookVisible != recipeBookVisible)
                {
                    recipeBookVisible = newRecipeBookVisible;
                    PlayerInventoryTabButton.this.updateGuiSize(screen.getGuiLeft(), screen.getGuiTop());
                }
            }
        };
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        tickCallback.run();

        graphics.blit(ClientHelpers.GUI_ICONS, getX(), getY(), 0, (float) textureU, (float) textureV, width, height, 256, 256);
        graphics.blit(ClientHelpers.GUI_ICONS, iconX, iconY, 16, 16, (float) tab.iconU, (float) tab.iconV, 16, 16, 256, 256);

        if (this.isHovered() && !this.active)
        {
            final Font font = Minecraft.getInstance().font;
            switch (tab)
            {
                case INVENTORY ->
                {
                    final Component title = Component.translatable("container.inventory");
                    graphics.renderTooltip(font, title, mouseX, mouseY);
                }
                case CALENDAR ->
                {
                    final Component title = Component.translatable("tfc.screen.calendar");
                    final Component hoverText = Calendars.CLIENT.getCalendarTimeAndDate();
                    graphics.renderComponentTooltip(font, List.of(title, hoverText), mouseX, mouseY);
                }
                case NUTRITION ->
                {
                    Player player = ClientHelpers.getPlayer();
                    Component components = Component.literal("");

                    if (player != null) {
                        if (player.getFoodData() instanceof TFCFoodData data) {
                            float avg = data.getNutrition().getAverageNutrition();
                            String formattedAvg = String.format("%.0f%%", avg * 100);

                            if (avg < 0.3f) {
                                components = Component.translatable("tfg.tooltip.nutrition.positive_average", Component.literal(formattedAvg).withStyle(ChatFormatting.RED));
                            } else if (avg < 0.6f) {
                                components = Component.translatable("tfg.tooltip.nutrition.positive_average", Component.literal(formattedAvg).withStyle(ChatFormatting.YELLOW));
                            } else if (avg < 0.95f) {
                                components = Component.translatable("tfg.tooltip.nutrition.positive_average", Component.literal(formattedAvg).withStyle(ChatFormatting.GREEN));
                            } else {
                                components = Component.translatable("tfg.tooltip.nutrition.positive_average", formattedAvg).withStyle(ChatFormatting.GOLD);
                            }
                        }
                    }

                    final Component title = Component.translatable("tfc.screen.nutrition");
                    graphics.renderComponentTooltip(font, List.of(title, components), mouseX, mouseY);
                }
                case CLIMATE ->
                {
                    final TemperatureDisplayStyle style = TFCConfig.CLIENT.climateTooltipStyle.get();
                    final Component title = Component.translatable("tfc.screen.climate");
                    final Component hoverText = Objects.requireNonNull(style.formatRange(ClimateRenderCache.INSTANCE.getTemperature()));
                    graphics.renderComponentTooltip(font, List.of(title, hoverText), mouseX, mouseY);
                }
                case BOOK ->
                {
                    final Component hoverText = Component.translatable("tfc.tab.field_guide");
                    graphics.renderTooltip(font, hoverText, mouseX, mouseY);
                }
            }
        }
    }

    public void updateGuiSize(int guiLeft, int guiTop)
    {
        setX(getX() + guiLeft - prevGuiLeft);
        setY(getY() + guiTop - prevGuiTop);

        this.iconX += guiLeft - prevGuiLeft;
        this.iconY += guiTop - prevGuiTop;

        prevGuiLeft = guiLeft;
        prevGuiTop = guiTop;
    }
}