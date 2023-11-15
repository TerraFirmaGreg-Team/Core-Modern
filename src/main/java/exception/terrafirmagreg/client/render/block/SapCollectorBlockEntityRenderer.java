package exception.terrafirmagreg.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import exception.terrafirmagreg.objects.entity.SapCollectorBlockEntity;
import exception.terrafirmagreg.util.ColorUtil;
import net.dries007.tfc.client.RenderHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

public class SapCollectorBlockEntityRenderer implements BlockEntityRenderer<SapCollectorBlockEntity> {
    public SapCollectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(SapCollectorBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        if (blockEntity.progress > 0 && blockEntity.currentRecipe != null) {
            poseStack.pushPose();

            Minecraft minecraft = Minecraft.getInstance();
            float pixHeight = 0.0625f;

            // fluid rendering starts here //
            //get info on type of fluid
            FluidStack fluidStack = blockEntity.currentRecipe.displayFluid;
            Fluid fluid = fluidStack.getFluid();

            //fluid brightness info
            int fluidBrightness = Math.max(combinedLightIn, fluid.getFluidType().getLightLevel());

            //fluid colour tint info
            IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
            int fluidTintColour = blockEntity.currentRecipe.fluidColor.isEmpty() ? renderProperties.getTintColor(fluidStack) : ColorUtil.getCacheColor(blockEntity.currentRecipe.fluidColor);
            float[] color = ColorUtil.getCacheColor(fluidTintColour);

            // No invisible fluids please
            color[3] = color[3] == 0.0f ? 1.0f : color[3];

            //fluid texture info
            TextureAtlasSprite stillFluidSprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(renderProperties.getStillTexture(fluidStack));
            VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.entityTranslucentCull(RenderHelpers.BLOCKS_ATLAS));

            //fluid render info
            Matrix4f lastPose = poseStack.last().pose();

            float progress = (float) blockEntity.progress / (float) blockEntity.currentRecipe.processingTime;
            float fluidY = (4.0f + (9f * progress)) / 16f;

            Direction dir = blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            float x1 = switch (dir) {
                case NORTH, SOUTH -> pixHeight * 13.5f;
                case EAST -> pixHeight * 10f;
                case WEST -> pixHeight * 15f;
                default -> 0f;
            };
            float x2 = switch (dir) {
                case NORTH, SOUTH -> pixHeight * 2.5f;
                case EAST -> pixHeight;
                case WEST -> pixHeight * 6f;
                default -> 0f;
            };
            float z1 = switch (dir) {
                case NORTH -> pixHeight * 15f;
                case SOUTH -> pixHeight * 10f;
                case EAST, WEST -> pixHeight * 13.5f;
                default -> 0f;
            };
            float z2 = switch (dir) {
                case NORTH -> pixHeight * 6f;
                case SOUTH -> pixHeight;
                case EAST, WEST -> pixHeight * 2.5f;
                default -> 0f;
            };

            float fluidSpriteU0 = stillFluidSprite.getU0() + (stillFluidSprite.getU1() - stillFluidSprite.getU0()) / 8;
            float fluidSpriteU1 = stillFluidSprite.getU1() - (stillFluidSprite.getU1() - stillFluidSprite.getU0()) / 8;
            float fluidSpriteV0 = stillFluidSprite.getV0() + (stillFluidSprite.getV1() - stillFluidSprite.getV0()) / 8;
            float fluidSpriteV1 = stillFluidSprite.getV1() - (stillFluidSprite.getV1() - stillFluidSprite.getV0()) / 8;
            //north-west
            vertexBuffer.vertex(lastPose, x1, fluidY, z1).color(color[0], color[1], color[2], color[3]).uv(fluidSpriteU0, fluidSpriteV1)
                    .overlayCoords(combinedOverlayIn).uv2(fluidBrightness).normal(0, 0, 1).endVertex();
            //south-west
            vertexBuffer.vertex(lastPose, x1, fluidY, z2).color(color[0], color[1], color[2], color[3]).uv(fluidSpriteU0, fluidSpriteV0)
                    .overlayCoords(combinedOverlayIn).uv2(fluidBrightness).normal(0, 0, 1).endVertex();
            //south-east
            vertexBuffer.vertex(lastPose, x2, fluidY, z2).color(color[0], color[1], color[2], color[3]).uv(fluidSpriteU1, fluidSpriteV0)
                    .overlayCoords(combinedOverlayIn).uv2(fluidBrightness).normal(0, 0, 1).endVertex();
            //north-east
            vertexBuffer.vertex(lastPose, x2, fluidY, z1).color(color[0], color[1], color[2], color[3]).uv(fluidSpriteU1, fluidSpriteV1)
                    .overlayCoords(combinedOverlayIn).uv2(fluidBrightness).normal(0, 0, 1).endVertex();

            poseStack.popPose();
        }
    }
}