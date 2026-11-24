package su.terrafirmagreg.core.client.renderer;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import su.terrafirmagreg.core.common.block.appliances.BakingDishBlock;
import su.terrafirmagreg.core.common.blockentity.appliances.BakingDishBlockEntity;

public class BakingDishBlockEntityRenderer implements BlockEntityRenderer<BakingDishBlockEntity> {

    public BakingDishBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BakingDishBlockEntity dish, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (dish.getLevel() == null) {
            return;
        }

        if (dish.getBlockState().getOptionalValue(BakingDishBlock.SEALED).orElse(false)) {
            return;
        }

        Direction facing = dish.getBlockState().getOptionalValue(BakingDishBlock.FACING).orElse(Direction.NORTH);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.getOpposite().toYRot()));
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        // Fluid Rendering
        float fluidY;
        FluidStack fluid = dish.getFluidTank();
        if (!fluid.isEmpty()) {
            float minX = 4.0F / 16.0F;
            float maxX = 12.0F / 16.0F;
            float minZ = 2.0F / 16.0F;
            float maxZ = 14.0F / 16.0F;

            float minY = 1.25F / 16.0F;
            float maxY = 2.5F / 16.0F;
            fluidY = Mth.map(fluid.getAmount(), 0, dish.getFluidCapacity(), minY, maxY);

            RenderHelpers.renderFluidFace(poseStack, fluid, buffer, minX, minZ, maxX, maxZ, fluidY, combinedOverlay, combinedLight);
        } else {
            fluidY = 1.25F / 16.0F;
        }

        // Item Rendering
        dish.getCapability(Capabilities.ITEM).ifPresent(cap -> {
            Minecraft mc = Minecraft.getInstance();
            long posSeed = dish.getBlockPos().asLong();

            float spacing = 0.8F;
            double[] xs = { 6.0 / 16.0, 10.0 / 16.0 };
            double[] zs = { (5.0 - spacing) / 16.0, (7.5 + (spacing / 2)) / 16.0, (10.0 + (spacing * 2)) / 16.0 };

            for (int i = 0; i < BakingDishBlockEntity.ITEM_SLOTS; i++) {
                ItemStack stack = cap.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    int col = i % 2;
                    int row = i / 2;
                    double x = xs[col];
                    double z = zs[row];
                    double y = fluidY + 0.03F;
                    float scale = 0.34F;

                    poseStack.pushPose();
                    poseStack.translate(x, y, z);
                    poseStack.scale(scale, scale, scale);
                    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(30.0F));

                    mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, buffer, dish.getLevel(), (int) posSeed + i);
                    poseStack.popPose();
                }
            }
        });

        poseStack.popPose();
    }
}
