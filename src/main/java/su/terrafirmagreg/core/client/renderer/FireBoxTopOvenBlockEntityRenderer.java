package su.terrafirmagreg.core.client.renderer;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import su.terrafirmagreg.core.common.block.appliances.FireBoxTopOvenBlock;
import su.terrafirmagreg.core.common.blockentity.appliances.FireBoxTopOvenBlockEntity;

public class FireBoxTopOvenBlockEntityRenderer implements BlockEntityRenderer<FireBoxTopOvenBlockEntity> {

    public FireBoxTopOvenBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FireBoxTopOvenBlockEntity oven, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (oven.getLevel() == null) {
            return;
        }

        Direction facing = oven.getBlockState().hasProperty(FireBoxTopOvenBlock.FACING)
                ? oven.getBlockState().getValue(FireBoxTopOvenBlock.FACING)
                : Direction.NORTH;
        int dirIndex = facing.get2DDataValue();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        oven.getCapability(Capabilities.ITEM).ifPresent(cap -> {
            for (int i = 0; i < 9; i++) {
                ItemStack item = cap.getStackInSlot(i);
                if (item.isEmpty()) {
                    item = cap.getStackInSlot(9 + i);
                }
                if (!item.isEmpty()) {
                    poseStack.pushPose();
                    Vec3 center = FireBoxTopOvenBlock.SLOT_CENTERS[dirIndex][i];
                    poseStack.translate(center.x, 0.0625D, center.z);
                    poseStack.scale(0.28f, 0.28f, 0.28f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(90F));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(180F + facing.toYRot()));

                    itemRenderer.renderStatic(item, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, buffer, oven.getLevel(), 0);
                    poseStack.popPose();
                }
            }
        });
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}
