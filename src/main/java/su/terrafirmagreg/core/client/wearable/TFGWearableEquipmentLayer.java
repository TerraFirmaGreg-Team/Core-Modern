package su.terrafirmagreg.core.client.wearable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGItems;

/**
 * Renders entity models for snorkel / flippers / snowshoes in armor slots.
 */
public class TFGWearableEquipmentLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public static final ResourceLocation SNORKEL_TEX = TFGCore.id("textures/entity/wearable/snorkel.png");
    public static final ResourceLocation FLIPPERS_TEX = TFGCore.id("textures/entity/wearable/flippers.png");
    public static final ResourceLocation SNOWSHOES_TEX = TFGCore.id("textures/entity/wearable/snowshoes.png");

    private final HumanoidModel<LivingEntity> snorkelModel;
    private final HumanoidModel<LivingEntity> flippersModel;
    private final HumanoidModel<LivingEntity> snowshoesModel;

    public TFGWearableEquipmentLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            EntityModelSet modelSet) {
        super(parent);
        this.snorkelModel = new TFGWearableHeadModel(
                modelSet.bakeLayer(TFGWearableRenderSetup.SNORKEL),
                RenderType::entityTranslucent);
        this.flippersModel = new TFGWearableLegsModel(modelSet.bakeLayer(TFGWearableRenderSetup.FLIPPERS));
        this.snowshoesModel = new TFGWearableLegsModel(modelSet.bakeLayer(TFGWearableRenderSetup.SNOWSHOES));
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (player.isInvisible()) {
            return;
        }
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.is(TFGItems.SNORKEL.get())) {
            renderAttachment(
                    player,
                    poseStack,
                    buffer,
                    packedLight,
                    head,
                    snorkelModel,
                    SNORKEL_TEX,
                    limbSwing,
                    limbSwingAmount,
                    partialTick,
                    ageInTicks,
                    netHeadYaw,
                    headPitch);
        }

        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        if (feet.is(TFGItems.FLIPPERS.get())) {
            renderAttachment(
                    player,
                    poseStack,
                    buffer,
                    packedLight,
                    feet,
                    flippersModel,
                    FLIPPERS_TEX,
                    limbSwing,
                    limbSwingAmount,
                    partialTick,
                    ageInTicks,
                    netHeadYaw,
                    headPitch);
        } else if (feet.is(TFGItems.SNOWSHOES.get())) {
            renderAttachment(
                    player,
                    poseStack,
                    buffer,
                    packedLight,
                    feet,
                    snowshoesModel,
                    SNOWSHOES_TEX,
                    limbSwing,
                    limbSwingAmount,
                    partialTick,
                    ageInTicks,
                    netHeadYaw,
                    headPitch);
        }
    }

    static void renderAttachment(
            LivingEntity entity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            ItemStack stack,
            HumanoidModel<LivingEntity> model,
            ResourceLocation texture,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        poseStack.pushPose();
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        followBodyRotations(entity, model);
        RenderType renderType = model.renderType(texture);
        VertexConsumer vertexBuilder = ItemRenderer.getFoilBuffer(buffer, renderType, false, stack.hasFoil());
        model.renderToBuffer(poseStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
    }

    @SuppressWarnings("unchecked")
    private static void followBodyRotations(LivingEntity entity, HumanoidModel<LivingEntity> model) {
        var dispatcher = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (dispatcher instanceof LivingEntityRenderer<?, ?> livingRenderer) {
            EntityModel<LivingEntity> entityModel = (EntityModel<LivingEntity>) livingRenderer.getModel();
            if (entityModel instanceof HumanoidModel<LivingEntity> biped) {
                biped.copyPropertiesTo(model);
            }
        }
    }
}
