package su.terrafirmagreg.core.mixins.client.firmalife;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.eerussianguy.firmalife.client.render.CompostTumblerBlockEntityRenderer;
import com.eerussianguy.firmalife.common.blockentities.CompostTumblerBlockEntity;
import com.eerussianguy.firmalife.common.blocks.CompostTumblerBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Girders;

@Mixin(value = CompostTumblerBlockEntityRenderer.class, remap = false)
public class CompostTumblerBlockEntityRendererMixin {

    @ModifyVariable(method = "render", at = @At("STORE"), name = "angle", remap = false)
    private float tfg$modifyAngle(float angle, CompostTumblerBlockEntity composter, float partialTicks) {
        if (composter.getLevel() != null) {
            Direction back = composter.getBlockState().getValue(CompostTumblerBlock.FACING).getOpposite();
            if (composter.getLevel().getBlockEntity(composter.getBlockPos().relative(back)) instanceof KineticBlockEntity kbe) {
                float stressAtBase = (float) BlockStressValues.getImpact(composter.getBlockState().getBlock());
                float theoreticalSpeed = Math.abs(kbe.getTheoreticalSpeed());
                boolean overstressed = kbe.isOverStressed() || (stressAtBase * theoreticalSpeed > 8.0001f);
                float speed = Math.abs(kbe.getSpeed());
                if (speed == 0 || overstressed) {
                    return 0;
                }
                speed = Math.min(speed, 32f);
                float tfcSpeed = speed * (float) Math.PI / 600f;
                return (composter.getLevel().getGameTime() + partialTicks) * tfcSpeed;
            }
        }
        return angle;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void tfg$renderAxle(CompostTumblerBlockEntity composter, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight, int combinedOverlay, CallbackInfo ci) {
        if (composter.getLevel() != null) {
            Direction back = composter.getBlockState().getValue(CompostTumblerBlock.FACING).getOpposite();
            if (composter.getLevel().getBlockEntity(composter.getBlockPos().relative(back)) instanceof KineticBlockEntity kbe) {
                Block axle = ForgeRegistries.BLOCKS.getValue(TFGBlocks_Girders.BRASS_BEAM.getId());
                float stressAtBase = (float) BlockStressValues.getImpact(composter.getBlockState().getBlock());
                float theoreticalSpeed = Math.abs(kbe.getTheoreticalSpeed());
                boolean overstressed = kbe.isOverStressed() || (stressAtBase * theoreticalSpeed > 8.0001f);
                float speed = Math.abs(kbe.getSpeed());
                if (axle != null && speed > 0 && !overstressed) {
                    BlockState state = axle.defaultBlockState();

                    poseStack.pushPose();
                    poseStack.translate(0, 0, 0.1f);
                    poseStack.translate(0.1f, 0.1f, 0.0f);
                    poseStack.scale(0.8f, 0.8f, 1.0f);
                    poseStack.translate(0.5f, 0.5f, 0.5f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    poseStack.translate(-0.5f, -0.5f, -0.5f);

                    CachedBuffers.block(state)
                            .light(combinedLight)
                            .overlay(combinedOverlay)
                            .renderInto(poseStack, buffers.getBuffer(RenderType.cutout()));
                    poseStack.popPose();
                }
            }
        }
    }
}
