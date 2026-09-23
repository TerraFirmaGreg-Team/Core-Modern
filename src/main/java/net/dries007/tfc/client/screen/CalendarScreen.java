/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

import net.dries007.tfc.client.screen.button.PlayerInventoryTabButton;
import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.compat.patchouli.PatchouliIntegration;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.SwitchInventoryTabPacket;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Month;

public class CalendarScreen extends TFCContainerScreen<Container>
{
    public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/player_calendar.png");

    public CalendarScreen(Container container, Inventory playerInv, Component name)
    {
        super(container, playerInv, name, BACKGROUND);
    }

    @Override
    public void init()
    {
        super.init();
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, false, false, PlayerInventoryTabButton.Tab.INVENTORY, button -> {
            playerInventory.player.containerMenu = playerInventory.player.inventoryMenu;
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode != null && mc.gameMode.isServerControlledInventory() && mc.player != null) {
                mc.setScreen(new CreativeModeInventoryScreen(playerInventory.player, mc.player.connection.enabledFeatures(), mc.options.operatorItemsTab().get()));
            } else {
                mc.setScreen(new InventoryScreen(playerInventory.player));
            }
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new SwitchInventoryTabPacket(PlayerInventoryTabButton.Tab.INVENTORY));
        }));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, true, false, PlayerInventoryTabButton.Tab.CALENDAR, button -> {}));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, false, false, PlayerInventoryTabButton.Tab.NUTRITION));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, false, false, PlayerInventoryTabButton.Tab.CLIMATE));
        PatchouliIntegration.ifEnabled(() -> addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, false, false, PlayerInventoryTabButton.Tab.BOOK)));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderLabels(graphics, mouseX, mouseY);

        String season = I18n.get("tfc.tooltip.calendar_season", I18n.get(Calendars.CLIENT.getCalendarMonthOfYear().getTranslationKey(Month.Style.SEASON)));
        String day = I18n.get("tfc.tooltip.calendar_day", Calendars.CLIENT.getCalendarDayOfYear().getString());
        String date = I18n.get("tfc.tooltip.calendar_date", Calendars.CLIENT.getCalendarTimeAndDate().getString());

        graphics.drawString(font, season, (imageWidth - font.width(season)) / 2, 25, 0x404040, false);
        graphics.drawString(font, day, (imageWidth - font.width(day)) / 2, 36, 0x404040, false);
        graphics.drawString(font, date, (imageWidth - font.width(date)) / 2, 47, 0x404040, false);
    }
}