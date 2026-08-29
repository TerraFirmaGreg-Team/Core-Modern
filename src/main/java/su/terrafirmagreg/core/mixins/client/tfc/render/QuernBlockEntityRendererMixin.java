package su.terrafirmagreg.core.mixins.client.tfc.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.render.CachedBuffers;
import net.dries007.tfc.client.render.blockentity.QuernBlockEntityRenderer;
import net.dries007.tfc.common.blockentities.QuernBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Girders;

/**
 * Mixin into {@link QuernBlockEntityRenderer} to add render handling for Create rotation support.
 */
@Mixin(value = QuernBlockEntityRenderer.class, remap = false)
public class QuernBlockEntityRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void tfg$renderAxle(QuernBlockEntity quern, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (quern.isConnectedToNetwork() && quern.hasHandstone()) {
            Block axle = ForgeRegistries.BLOCKS.getValue(TFGBlocks_Girders.BRASS_BEAM.getId());
            if (axle != null) {
                BlockState state = axle.defaultBlockState();
                stack.pushPose();
                stack.translate(0.1f, 0.6f, 0.1f);
                stack.scale(0.8f, 0.5f, 0.8f);
                CachedBuffers.block(state)
                        .light(packedLight)
                        .overlay(packedOverlay)
                        .renderInto(stack, bufferSource.getBuffer(RenderType.cutout()));
                stack.popPose();
            }
        }
    }
}
