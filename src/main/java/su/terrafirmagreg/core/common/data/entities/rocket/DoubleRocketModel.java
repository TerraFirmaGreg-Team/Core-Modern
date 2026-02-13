package su.terrafirmagreg.core.common.data.entities.rocket;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;

import earth.terrarium.adastra.common.entities.vehicles.Rocket;

import su.terrafirmagreg.core.TFGCore;

public class DoubleRocketModel<T extends Rocket> extends EntityModel<T> {
    public static final ModelLayerLocation TIER_1_DOUBLE_LAYER = new ModelLayerLocation(TFGCore.id("tier_1_double_rocket"), "main");
    private final ModelPart root;

    public DoubleRocketModel(ModelPart root) {
        this.root = root.getChild("main");
    }

    /*public static LayerDefinition createTier1DoubleLayer() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition rocket = modelPartData.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 25.0F, 0.0F));
    
        return LayerDefinition.create(modelData, 128, 128);
    }*/

    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }
}
