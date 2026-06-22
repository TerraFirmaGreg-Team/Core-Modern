package su.terrafirmagreg.core.common.entity.slime;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import su.terrafirmagreg.core.TFGCore;

public class TFGSlimeModel<T extends TFGSlime> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            TFGCore.id("slime"), "main");

    private final ModelPart root;

    public TFGSlimeModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("inner", CubeListBuilder.create().texOffs(44, 15).addBox(-6.0F, 15.0F, -6.0F, 11.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(80, 0).addBox(-5.0F, 14.0F, -5.0F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -3.0F, 0.5F));
        partdefinition.addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(52, -14).addBox(-0.5F, -26.0F, -3.0F, 0.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.5F, 24.0F, -0.5F));
        partdefinition.addOrReplaceChild("other_antenna", CubeListBuilder.create().texOffs(44, 23).addBox(0.5F, -26.0F, -3.0F, 0.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.5F, 24.0F, -0.5F, 0.0F, -1.5708F, 0.0F));
        partdefinition
                .addOrReplaceChild("cross", CubeListBuilder.create().texOffs(80, -2).addBox(0.0F, -5.0F, -6.0F, 0.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.0F, -0.7854F, 0.0F))
                .addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(80, -2).addBox(0.0F, -9.0F, -6.0F, 0.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("outer", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 12.0F, -6.0F, 13.0F, 11.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).addBox(-6.0F, 10.0F, -5.0F, 11.0F, 14.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, -0.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static LayerDefinition createFaceLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("outer", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 12.0F, -6.0F, 13.0F, 11.0F, 13.0F, new CubeDeformation(0.01F))
                .texOffs(0, 24).addBox(-6.0F, 10.0F, -5.0F, 11.0F, 14.0F, 11.0F, new CubeDeformation(0.01F)), PartPose.offset(0.5F, 0.0F, -0.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public ModelPart root() {
        return this.root;
    }
}
