package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.negodya1.vintageimprovements.VintageItems;
import com.negodya1.vintageimprovements.content.kinetics.helve_hammer.HelveBlockEntity;
import com.negodya1.vintageimprovements.content.kinetics.helve_hammer.HelveItemsRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Moves the position of the items so they are aligned with the TFC anvils.
 */
@Mixin(value = HelveItemsRenderer.class, remap = false)
public class HelveItemsRendererMixin {

    @Inject(method = "renderItems", at = @At("HEAD"), cancellable = true)
    protected void tfg$renderItems(HelveBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
            CallbackInfo ci) {
        boolean input = !be.inputInv.getStackInSlot(0).isEmpty();
        boolean output = !be.outputInv.getStackInSlot(0).isEmpty();

        if (!input && !output) {
            ci.cancel();
            return;
        }

        int j = 0;

        if (input) {
            for (int i = 0; i < be.inputInv.getSlots(); i++) {
                ItemStack stack = be.inputInv.getStackInSlot(i);
                if (stack.isEmpty())
                    continue;

                ms.pushPose();

                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

                ms.translate(.5, -.3, .5);

                ms.mulPose(Axis.YP.rotationDegrees(j * 60));

                ms.scale(.33f, .33f, .33f);
                ms.mulPose(Axis.XP.rotationDegrees(90));
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(),
                        0);
                j++;

                ms.popPose();
            }
        }

        for (int i = 0; i < be.getBlockedSlots(); i++) {
            ItemStack stack = new ItemStack(VintageItems.HELVE_HAMMER_SLOT_COVER.get(), 1);
            ms.pushPose();

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            ms.translate(.5, -.31, .5);

            ms.mulPose(Axis.YP.rotationDegrees(j * 60));
            ms.translate(-.25, 0, 0);

            ms.scale(.33f, .33f, .33f);
            ms.mulPose(Axis.XP.rotationDegrees(90));
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
            j++;
            ms.popPose();
        }

        if (output) {
            for (int i = 0; i < be.outputInv.getSlots(); i++) {
                ItemStack stack = be.outputInv.getStackInSlot(i);
                if (stack.isEmpty())
                    continue;

                ms.pushPose();

                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

                ms.translate(.5, -.29, .5);

                ms.mulPose(Axis.YP.rotationDegrees(j * 60));
                ms.translate(-.20, 0, 0);

                ms.scale(.33f, .33f, .33f);
                ms.mulPose(Axis.XP.rotationDegrees(90));
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(),
                        0);
                j++;

                ms.popPose();
            }
        }

        ci.cancel();
    }
}
