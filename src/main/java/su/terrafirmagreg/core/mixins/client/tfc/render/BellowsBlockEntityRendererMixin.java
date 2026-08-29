package su.terrafirmagreg.core.mixins.client.tfc.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.createmod.catnip.render.CachedBuffers;
import net.dries007.tfc.client.render.blockentity.BellowsBlockEntityRenderer;
import net.dries007.tfc.common.blockentities.BellowsBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Girders;

/**
 * Mixin into {@link BellowsBlockEntityRenderer} to add render handling for Create rotation support.
 * Also fixes the infamous stretching bug.
 */
@Mixin(value = BellowsBlockEntityRenderer.class, remap = false)
public class BellowsBlockEntityRendererMixin {

    @ModifyVariable(method = "drawMiddle", at = @At(value = "STORE"), name = "change")
    private float tfg$fixBellowsStretching(float change) {
        return Math.min(change, 0.375f);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", remap = true))
    private void tfg$renderAxle(BellowsBlockEntity bellows, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (bellows.isConnectedToNetwork()) {
            Block axle = ForgeRegistries.BLOCKS.getValue(TFGBlocks_Girders.BRASS_BEAM.getId());
            if (axle != null) {
                BlockState state = axle.defaultBlockState();

                poseStack.pushPose();
                poseStack.translate(0.1f, 0.1f, 0.1f);
                poseStack.scale(0.8f, 0.8f, 1.0f);
                poseStack.translate(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.translate(-0.5f, -0.5f, -0.5f);

                CachedBuffers.block(state)
                        .light(packedLight)
                        .overlay(packedOverlay)
                        .renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));
                poseStack.popPose();
            }
        }
    }
}
