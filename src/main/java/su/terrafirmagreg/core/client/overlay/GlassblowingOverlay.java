/*
 * File originating from TerraFirmaCraft
 * Licensed under the EUPL, Version 1.2.
 * You may get a copy of the License at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.client.overlay;

import java.util.Locale;
import java.util.Objects;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.GlassBasinBlock;
import net.dries007.tfc.common.capabilities.glass.GlassOperation;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.util.Helpers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;
import su.terrafirmagreg.core.utils.TFGHelpers;

public class GlassblowingOverlay {

    private static final TagKey<Item> TFC_GLASS_BLOWPIPES = TagKey.create(ForgeRegistries.Keys.ITEMS,
            ResourceLocation.fromNamespaceAndPath("tfc", "glass_blowpipes"));

    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, width, height) -> {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }
        final Player player = minecraft.player;
        if (player != null) {
            final boolean holdingGlassBlowpipe = Helpers.isItem(player.getMainHandItem(), TFC_GLASS_BLOWPIPES) || Helpers.isItem(player.getOffhandItem(), TFC_GLASS_BLOWPIPES);
            if (holdingGlassBlowpipe) {
                render(minecraft, graphics);
            }
        }
    };

    public static Direction getTargetedFace() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.hitResult instanceof BlockHitResult block) {
            return block.getDirection();
        }
        return null;
    }

    public static String getTranslationId(GlassOperation operation) {
        return "tfc.enum.glassoperation." + operation.name().toLowerCase(Locale.ROOT);
    }

    public static boolean render(Minecraft minecraft, GuiGraphics graphics) {
        final Level level = minecraft.level;
        final Player player = minecraft.player;
        final BlockPos targetedPos = ClientHelpers.getTargetedPos();
        final Direction targetedFace = getTargetedFace();
        final int x = graphics.guiWidth() / 2 + 3;
        final int y = graphics.guiHeight() / 2 + 8;

        if (player != null) {
            ItemStack held = player.getMainHandItem();
            ItemStack otherItem = player.getOffhandItem();

            if (!Helpers.isItem(player.getMainHandItem(), TFC_GLASS_BLOWPIPES)) {
                held = player.getOffhandItem();
                otherItem = player.getMainHandItem();
            }

            ChatFormatting color = ChatFormatting.WHITE;
            if (Heat.getHeat(HeatCapability.getTemperature(held)) != null) {
                color = Objects.requireNonNull(Heat.getHeat(HeatCapability.getTemperature(held))).getColor();
            }

            if (player.getCooldowns().isOnCooldown(held.getItem())) {
                Component line = Component.translatable("tfg.tooltip.complete");
                drawCenteredText(minecraft, graphics, line, x, y);
                return true;
            }

            if (level != null && targetedPos != null && targetedFace != null) {
                final BlockState targetedState = level.getBlockState(targetedPos);
                final BlockPos center = targetedPos.relative(targetedFace);
                if (GlassBasinBlock.isValid(level, center)) {
                    Component line = Component.translatable(getTranslationId(GlassOperation.BASIN_POUR)).withStyle(color);
                    drawCenteredText(minecraft, graphics, line, x, y);
                    return true;
                }
                if (targetedFace == Direction.UP && Helpers.isBlock(targetedState, TFCTags.Blocks.GLASS_POURING_TABLE) && level.getBlockState(targetedPos.above()).isAir()) {
                    Component line = Component.translatable(getTranslationId(GlassOperation.TABLE_POUR)).withStyle(color);
                    drawCenteredText(minecraft, graphics, line, x, y);
                    return true;
                }
            }

            final GlassOperation op = GlassOperation.get(otherItem, player);
            if (op != null) {
                StringBuilder progress = new StringBuilder();
                if (player.isUsingItem()) {
                    int tally = (TFGHelpers.getGlassworkingDuration(player) - player.getUseItemRemainingTicks()) / 8 + 1;
                    progress.append("|".repeat(tally));
                }

                Component line = Component.translatable(getTranslationId(op)).append(" " + progress).withStyle(color);

                drawCenteredText(minecraft, graphics, line, x, y);
                return true;
            }
        }

        return false;
    }

    private static void drawCenteredText(Minecraft minecraft, GuiGraphics graphics, Component text, int x, int y) {
        final int textWidth = minecraft.font.width(text) / 2;
        graphics.drawString(minecraft.font, text, x - textWidth, y, 0xCCCCCC, true);
    }
}
