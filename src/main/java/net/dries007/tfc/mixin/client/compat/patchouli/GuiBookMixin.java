/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.client.compat.patchouli;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.common.book.Book;

import net.dries007.tfc.client.screen.button.PlayerInventoryTabButton;
import net.dries007.tfc.compat.patchouli.PatchouliIntegration;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.SwitchInventoryTabPacket;
import net.dries007.tfc.util.Helpers;

@Mixin(GuiBook.class)
public abstract class GuiBookMixin extends Screen
{
    @Shadow(remap = false)
    public int bookLeft;

    @Shadow(remap = false)
    public int bookTop;

    @Final
    @Shadow(remap = false)
    public Book book;

    protected GuiBookMixin(Component title)
    {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void injectTabButtons(CallbackInfo ci)
    {
        if (book != null && (book.id.equals(PatchouliIntegration.BOOK_ID) || book.id.equals(Helpers.resourceLocation("tfg", "field_guide"))))
        {
            final Minecraft mc = Minecraft.getInstance();
            if (mc.player != null)
            {
                final Inventory playerInventory = mc.player.getInventory();

                addRenderableWidget(new PlayerInventoryTabButton(bookLeft, bookTop, false, true, PlayerInventoryTabButton.Tab.INVENTORY, button -> {
                    playerInventory.player.containerMenu = playerInventory.player.inventoryMenu;
                    mc.setScreen(new InventoryScreen(playerInventory.player));
                    PacketHandler.send(PacketDistributor.SERVER.noArg(), new SwitchInventoryTabPacket(PlayerInventoryTabButton.Tab.INVENTORY));
                }));
                addRenderableWidget(new PlayerInventoryTabButton(bookLeft, bookTop, false, true, PlayerInventoryTabButton.Tab.CALENDAR));
                addRenderableWidget(new PlayerInventoryTabButton(bookLeft, bookTop, false, true, PlayerInventoryTabButton.Tab.NUTRITION));
                addRenderableWidget(new PlayerInventoryTabButton(bookLeft, bookTop, false, true, PlayerInventoryTabButton.Tab.CLIMATE));
                addRenderableWidget(new PlayerInventoryTabButton(bookLeft, bookTop, true, true, PlayerInventoryTabButton.Tab.BOOK, button -> {}));
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void injectRenderConsistentBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci)
    {
        if (book != null && (book.id.equals(PatchouliIntegration.BOOK_ID) || book.id.equals(Helpers.resourceLocation("tfg", "field_guide"))))
        {
            renderBackground(graphics);
        }
    }
}
