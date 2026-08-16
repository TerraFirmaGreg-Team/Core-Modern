/*
 * Originally from [Create Diesel Generators] (https://github.com/george8188625/Create-Diesel-Generators)
 * Licensed under the MIT license.
 */

package su.terrafirmagreg.core.common.block.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import electrolyte.greate.content.kinetics.base.TieredShaftRenderer;

import su.terrafirmagreg.core.common.data.TFGPartialModels;

public class CombustionEngineRenderer extends TieredShaftRenderer<CombustionEngineBlockEntity> {

    public CombustionEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CombustionEngineBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        int angle = (int) (Math.abs(KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), KineticBlockEntityRenderer.getRotationAxisOf(be)) * 180 / Math.PI) * 3 % 360) / 36;

        if (be.getBlockState().getValue(CombustionEngineBlock.FACING).getAxis().isHorizontal()) {
            PartialModel pistonModel = switch (angle) {
                case 2, 9 -> TFGPartialModels.ENGINE_PISTONS_1;
                case 3, 8 -> TFGPartialModels.ENGINE_PISTONS_2;
                case 4, 7 -> TFGPartialModels.ENGINE_PISTONS_3;
                case 5, 6 -> TFGPartialModels.ENGINE_PISTONS_4;
                default -> TFGPartialModels.ENGINE_PISTONS_0;
            };

            CachedBuffers
                    .partial(pistonModel, be.getBlockState())
                    .center()
                    .rotateYDegrees(be.getBlockState().getValue(CombustionEngineBlock.FACING).toYRot()).uncenter()
                    .light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        } else {
            PartialModel pistonModel = switch (angle) {
                case 2, 9 -> TFGPartialModels.ENGINE_PISTONS_VERTICAL_1;
                case 3, 8 -> TFGPartialModels.ENGINE_PISTONS_VERTICAL_2;
                case 4, 7 -> TFGPartialModels.ENGINE_PISTONS_VERTICAL_3;
                case 5, 6 -> TFGPartialModels.ENGINE_PISTONS_VERTICAL_4;
                default -> TFGPartialModels.ENGINE_PISTONS_VERTICAL_0;
            };

            CachedBuffers
                    .partial(pistonModel, be.getBlockState())
                    .center().rotateYDegrees(
                            be.getBlockState().getValue(CombustionEngineBlock.FACING) == Direction.DOWN ? 180 : 270)
                    .rotateZDegrees(be.getBlockState().getValue(CombustionEngineBlock.FACING) == Direction.DOWN ? 180 : 0).uncenter()
                    .light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }
}
