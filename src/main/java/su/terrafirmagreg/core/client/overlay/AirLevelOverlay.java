package su.terrafirmagreg.core.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.ClientHypoxiaHandler;
import su.terrafirmagreg.core.common.environment.HypoxiaHandler;

/**
 * Same code as ForgeGui#renderAir
 * the bar fills from the right and locked bubbles take the slots at its left end.
 * Replace the Vanilla Air Bar with our own custom one that can handle Hypoxia, SpaceSuit reserves and also works on Earth
 * By priority you will the see Hypoxia Bar (You lost breath in a Vacuum and you have to fill back a debt to get your full breath bar back
 * If you have a SpaceSuit equipped (AdAstra SpaceSuitItem) your Oxygen Bar is replaced by the Oxygen Tank Icon to track your reserve
 * Also you have normal breath replicated
 * HypoxiaHandler and DimensionEnvironment can be tweaked for the bar to be shown differently as the new bar works through Vacuum of the dimension and if Air is available
 */
public class AirLevelOverlay {

    /** Look at the image: one row per BubbleStyle, one column per bubble state */
    private static final ResourceLocation OXYGEN_BUBBLES = TFGCore.id("textures/gui/icons/oxygen_bubbles.png");
    private static final int BUBBLE_SIZE = 9;
    private static final int TEXTURE_WIDTH = 3 * BUBBLE_SIZE;
    private static final int TEXTURE_HEIGHT = 4 * BUBBLE_SIZE;

    private static final int FULL = 0;
    private static final int PARTIAL = 1;
    private static final int EMPTY = 2;

    /** When the SpaceSuit bar starts blinking */
    private static final float LOW_SUIT_RESERVE = 0.1f;
    /** The blinking frequency */
    private static final int BLINK_TICKS = 10;

    private enum BubbleStyle {
        SUIT(0, true),
        HYPOXIA(1, true),
        LOCKED(2, false),
        NORMAL(3, false);

        private final int row;
        private final boolean showsEmpty;

        BubbleStyle(int row, boolean showsEmpty) {
            this.row = row;
            this.showsEmpty = showsEmpty;
        }
    }

    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, width, height) -> {
        final Minecraft minecraft = gui.getMinecraft();
        if (minecraft.options.hideGui || !gui.shouldDrawSurvivalElements()) {
            return;
        }
        if (minecraft.getCameraEntity() instanceof Player player) {
            gui.setupOverlayRenderState(true, false);
            render(gui, graphics, player, width, height);
        }
    };

    private static void render(ForgeGui gui, GuiGraphics graphics, Player player, int width, int height) {
        final int maxAir = player.getMaxAirSupply();
        final int debt = ClientHypoxiaHandler.getOxygenDebt();
        final int locked = HypoxiaHandler.getLockedBubbles(debt);
        final ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        final boolean suitWorn = SpaceSuitItem.hasFullSet(player) && chest.getItem() instanceof SpaceSuitItem;

        // What the bar shows in air units (0 to maxAir)
        final BubbleStyle style;
        final int amount;
        if (debt > 0) {
            style = BubbleStyle.HYPOXIA;
            amount = player.getAirSupply();
        } else if (suitWorn && (SpaceSuitItem.hasOxygen(player) || player.getAirSupply() >= maxAir)) {
            style = BubbleStyle.SUIT;
            amount = getSuitAir(player, chest, maxAir);
        } else {
            style = BubbleStyle.NORMAL;
            amount = player.getAirSupply();
        }

        // Vanilla only shows the bar underwater or when not full. Also shown in debt -> always with a full space suit
        if (!suitWorn && locked == 0 && amount >= maxAir && !player.isEyeInFluid(FluidTags.WATER)) {
            return;
        }

        gui.getMinecraft().getProfiler().push("air");
        RenderSystem.enableBlend();
        final int left = width / 2 + 91;
        final int top = height - gui.rightHeight;

        final int full = Mth.ceil((double) (amount - 2) * HypoxiaHandler.AIR_BUBBLES / maxAir);
        final int filled = Math.max(0, Mth.ceil((double) amount * HypoxiaHandler.AIR_BUBBLES / maxAir));
        final int firstLocked = Math.max(HypoxiaHandler.AIR_BUBBLES - locked, filled);
        final boolean blinkOff = style == BubbleStyle.SUIT && amount < maxAir * LOW_SUIT_RESERVE && (player.tickCount / BLINK_TICKS) % 2 == 1;

        for (int slot = 0; slot < HypoxiaHandler.AIR_BUBBLES; ++slot) {
            if (slot >= firstLocked) {
                drawBubble(graphics, left, top, slot, BubbleStyle.LOCKED, FULL);
            } else if (slot < filled && !blinkOff) {
                drawBubble(graphics, left, top, slot, style, slot < full ? FULL : PARTIAL);
            } else if (style.showsEmpty) {
                drawBubble(graphics, left, top, slot, style, EMPTY);
            }
        }

        gui.rightHeight += 10;
        RenderSystem.disableBlend();
        gui.getMinecraft().getProfiler().pop();
    }

    /**
     * Space suit tank converted to air units to replace Ad Astra's oxygen tank overlay
     */
    private static int getSuitAir(Player player, ItemStack chest, int maxAir) {
        if (!(chest.getItem() instanceof SpaceSuitItem suit)) {
            return 0;
        }
        long capacity = suit.getFluidContainer(chest).getTankCapacity(0);
        if (capacity <= 0) {
            return 0;
        }
        return (int) Math.min(maxAir, SpaceSuitItem.getOxygenAmount(player) * maxAir / capacity);
    }

    /**
     * Draws one bubble of the bar with slot 0 being the rightmost.
     */
    private static void drawBubble(GuiGraphics graphics, int left, int top, int slot, BubbleStyle style, int column) {
        graphics.blit(OXYGEN_BUBBLES, left - slot * 8 - 9, top, column * BUBBLE_SIZE, style.row * BUBBLE_SIZE, BUBBLE_SIZE, BUBBLE_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
