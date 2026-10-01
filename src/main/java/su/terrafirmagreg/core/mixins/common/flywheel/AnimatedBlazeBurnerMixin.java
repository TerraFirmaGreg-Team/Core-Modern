package su.terrafirmagreg.core.mixins.common.flywheel;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.eerussianguy.beneath.common.blocks.BeneathBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.minecraft.client.gui.GuiGraphics;

import su.terrafirmagreg.core.common.block.GasBurnerBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;

@Mixin(value = AnimatedBlazeBurner.class, remap = false)
public abstract class AnimatedBlazeBurnerMixin extends AnimatedKinetics {

    @Shadow
    private BlazeBurnerBlock.HeatLevel heatLevel;

    /**
     * @author Exception & Redeix
     * @reason Render a forge & gas burner instead of the Create blaze burner.
     */
    @Overwrite
    public void draw(@NotNull GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));

        int scale = 23;

        int heatState = 0;
        if (heatLevel == BlazeBurnerBlock.HeatLevel.SEETHING) {
            heatState = 6;
        } else if (heatLevel == BlazeBurnerBlock.HeatLevel.KINDLED) {
            heatState = 3;
        }

        int cycle = (int) ((System.currentTimeMillis() / 1000) % 3);

        switch (cycle) {
            case 1:
                for (int xOff = -1; xOff <= 1; xOff++) {
                    for (int zOff = -1; zOff <= 1; zOff++) {
                        blockElement(BeneathBlocks.HELLFORGE.get().defaultBlockState().setValue(CharcoalForgeBlock.HEAT, heatState))
                                .atLocal(xOff, 1.65, zOff)
                                .scale(scale)
                                .render(graphics);
                    }
                }
                break;
            case 2:
                blockElement(TFGBlocks.GAS_BURNER.get().defaultBlockState().setValue(GasBurnerBlock.HEAT, heatState).setValue(GasBurnerBlock.LIT, true))
                        .atLocal(0, 1.65, 0)
                        .scale(scale)
                        .render(graphics);
                break;
            default:
                blockElement(TFCBlocks.CHARCOAL_FORGE.get().defaultBlockState().setValue(CharcoalForgeBlock.HEAT, heatState))
                        .atLocal(0, 1.65, 0)
                        .scale(scale)
                        .render(graphics);
                break;
        }

        matrixStack.popPose();
    }

}
