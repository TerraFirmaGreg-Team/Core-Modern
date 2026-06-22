package su.terrafirmagreg.tfcambiental.curios;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import su.terrafirmagreg.tfcambiental.item.ClothesItem;

public class ClothesCurioRenderer implements ICurioRenderer {
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (slotContext.entity() instanceof AbstractClientPlayer player && stack.getItem() instanceof ClothesItem clothesItem) {
            if (slotContext.entity().getItemBySlot(clothesItem.getType().getSlot()).isEmpty()) {
                boolean isSlim = player.getModelName().equals("slim");
                HumanoidModel<T> model = new HumanoidModel<T>(
                        Minecraft.getInstance().getEntityModels().bakeLayer(
                                isSlim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR));

                CurioClothesLayer layer = new CurioClothesLayer(renderLayerParent, model, model);

                poseStack.pushPose();

                layer.render(stack, poseStack, renderTypeBuffer, light, slotContext.entity());

                poseStack.popPose();
            }
        }

    }
}
