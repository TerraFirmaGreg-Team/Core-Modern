package su.terrafirmagreg.core.common.data.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LargeNestBoxBlockEntityRenderer implements BlockEntityRenderer<LargeNestBoxBlockEntity> {

    @Override
    public void render(LargeNestBoxBlockEntity nestBox, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
    {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        nestBox.getCapability(Capabilities.ITEM).ifPresent(cap -> {
            float timeD = RenderHelpers.itemTimeRotation();
            poseStack.translate(0.5D, 0.25D, 0.5D);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            for (int i = 0; i < cap.getSlots(); i++)
            {
                ItemStack stack = cap.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                poseStack.pushPose();
                poseStack.translate((i % 2 == 0 ? -1 : 1) * 0.33f, 0, (i < 2 ? -1 : 1) * 0.33f);
                poseStack.mulPose(Axis.YP.rotationDegrees(timeD));
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, buffer, nestBox.getLevel(), 0);
                poseStack.popPose();
            }
        });
    }

}
