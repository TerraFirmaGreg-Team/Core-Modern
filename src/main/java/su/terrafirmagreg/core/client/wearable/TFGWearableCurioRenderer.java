package su.terrafirmagreg.core.client.wearable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class TFGWearableCurioRenderer implements ICurioRenderer {

    private final WearableType type;
    private HumanoidModel<LivingEntity> model;

    private TFGWearableCurioRenderer(WearableType type) {
        this.type = type;
    }

    public static TFGWearableCurioRenderer snorkel() {
        return new TFGWearableCurioRenderer(WearableType.SNORKEL);
    }

    public static TFGWearableCurioRenderer flippers() {
        return new TFGWearableCurioRenderer(WearableType.FLIPPERS);
    }

    public static TFGWearableCurioRenderer snowshoes() {
        return new TFGWearableCurioRenderer(WearableType.SNOWSHOES);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffer,
            int packedLight,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        LivingEntity entity = slotContext.entity();
        TFGWearableEquipmentLayer.renderAttachment(
                entity,
                poseStack,
                buffer,
                packedLight,
                stack,
                getModel(),
                type.texture,
                limbSwing,
                limbSwingAmount,
                partialTick,
                ageInTicks,
                netHeadYaw,
                headPitch);
    }

    private HumanoidModel<LivingEntity> getModel() {
        if (model == null) {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            model = type.createModel(modelSet);
        }
        return model;
    }

    private enum WearableType {
        SNORKEL(TFGWearableEquipmentLayer.SNORKEL_TEX) {
            @Override
            HumanoidModel<LivingEntity> createModel(EntityModelSet modelSet) {
                return new TFGWearableHeadModel(
                        modelSet.bakeLayer(TFGWearableRenderSetup.SNORKEL),
                        RenderType::entityTranslucent);
            }
        },
        FLIPPERS(TFGWearableEquipmentLayer.FLIPPERS_TEX) {
            @Override
            HumanoidModel<LivingEntity> createModel(EntityModelSet modelSet) {
                return new TFGWearableLegsModel(modelSet.bakeLayer(TFGWearableRenderSetup.FLIPPERS));
            }
        },
        SNOWSHOES(TFGWearableEquipmentLayer.SNOWSHOES_TEX) {
            @Override
            HumanoidModel<LivingEntity> createModel(EntityModelSet modelSet) {
                return new TFGWearableLegsModel(modelSet.bakeLayer(TFGWearableRenderSetup.SNOWSHOES));
            }
        };

        private final ResourceLocation texture;

        WearableType(ResourceLocation texture) {
            this.texture = texture;
        }

        abstract HumanoidModel<LivingEntity> createModel(EntityModelSet modelSet);
    }
}
