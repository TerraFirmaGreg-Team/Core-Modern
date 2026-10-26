package su.terrafirmagreg.core.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import earth.terrarium.adastra.api.systems.PlanetData;
import earth.terrarium.adastra.client.utils.ClientData;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;

import su.terrafirmagreg.core.TFGCore;

/**
 * Low space suit oxygen
 * Add Pressure here in the future
 */
public class VignetteOverlay {

    private static final ResourceLocation LOW_OXYGEN = TFGCore.id("textures/gui/hypoxia_overlay.png");

    private static final float LOW_OXYGEN_START = 0.2f;
    private static final float MAX_OPACITY = 0.8f;

    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, width, height) -> {
        final Minecraft minecraft = gui.getMinecraft();
        if (minecraft.options.hideGui || !(minecraft.getCameraEntity() instanceof Player player)) {
            return;
        }
        final float opacity = getLowOxygenOpacity(player);
        if (opacity > 0) {
            drawVignette(graphics, LOW_OXYGEN, opacity, width, height);
        }
    };

    private static float getLowOxygenOpacity(Player player) {
        final ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!SpaceSuitItem.hasFullSet(player) || !(chest.getItem() instanceof SpaceSuitItem suit)) {
            return 0;
        }
        final PlanetData localData = ClientData.getLocalData();
        if (localData != null && localData.oxygen()) {
            return 0;
        }
        final long capacity = suit.getFluidContainer(chest).getTankCapacity(0);
        if (capacity <= 0) {
            return 0;
        }
        final float ratio = (float) SpaceSuitItem.getOxygenAmount(player) / capacity;
        return Mth.clamp((LOW_OXYGEN_START - ratio) / LOW_OXYGEN_START, 0f, 1f) * MAX_OPACITY;
    }

    private static void drawVignette(GuiGraphics graphics, ResourceLocation texture, float opacity, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        graphics.setColor(1f, 1f, 1f, opacity);
        graphics.blit(texture, 0, 0, -90, 0f, 0f, width, height, width, height);
        graphics.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
