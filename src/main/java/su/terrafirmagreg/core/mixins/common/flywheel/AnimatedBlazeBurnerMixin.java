package su.terrafirmagreg.core.mixins.common.flywheel;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(value = AnimatedBlazeBurner.class, remap = false)
public abstract class AnimatedBlazeBurnerMixin extends AnimatedKinetics {

    @Shadow
    private BlazeBurnerBlock.HeatLevel heatLevel;

    /**
     * @author Exception
     * @reason Рендерить блок угольной печи вместо блейзовой печи Create.
     */
    @Overwrite
    public void draw(@NotNull GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));

        int scale = 23;

        int forgeHeatLevel = 0;
        if (heatLevel == BlazeBurnerBlock.HeatLevel.SEETHING) {
            forgeHeatLevel = 7;
        } else if (heatLevel == BlazeBurnerBlock.HeatLevel.KINDLED) {
            forgeHeatLevel = 3;
        }

        blockElement(
                TFCBlocks.CHARCOAL_FORGE.get().defaultBlockState().setValue(CharcoalForgeBlock.HEAT, forgeHeatLevel))
                .atLocal(0, 1.65, 0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }

}
