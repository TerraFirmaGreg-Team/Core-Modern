package su.terrafirmagreg.core.client.wearable;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Leg attachment meshes for flippers and snowshoes.
 */
public class TFGWearableLegsModel extends HumanoidModel<LivingEntity> {

    public TFGWearableLegsModel(ModelPart part) {
        super(part, RenderType::entityCutoutNoCull);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(leftLeg, rightLeg);
    }

    public static MeshDefinition createLegs(float delta, CubeListBuilder leftLeg, CubeListBuilder rightLeg) {
        CubeDeformation deformation = new CubeDeformation(delta);
        MeshDefinition mesh = createMesh(CubeDeformation.NONE, 0);
        mesh.getRoot().addOrReplaceChild(
                "left_leg",
                leftLeg.texOffs(0, 0).addBox(-2, 0, -2, 4, 12, 4, deformation),
                PartPose.offset(1.9F, 12, 0));
        mesh.getRoot().addOrReplaceChild(
                "right_leg",
                rightLeg.texOffs(16, 0).addBox(-2, 0, -2, 4, 12, 4, deformation),
                PartPose.offset(-1.9F, 12, 0));
        return mesh;
    }

    public static MeshDefinition createFlippers() {
        CubeListBuilder leftLeg = CubeListBuilder.create();
        CubeListBuilder rightLeg = CubeListBuilder.create();
        leftLeg.texOffs(0, 16);
        leftLeg.addBox(-2, 11.5F, -16, 9, 0, 20);
        rightLeg.texOffs(0, 36);
        rightLeg.addBox(-7, 11.5F, -16, 9, 0, 20);
        return createLegs(0.5F, leftLeg, rightLeg);
    }

    public static MeshDefinition createSnowshoes() {
        CubeListBuilder leftLeg = CubeListBuilder.create();
        CubeListBuilder rightLeg = CubeListBuilder.create();
        leftLeg.texOffs(0, 16);
        leftLeg.addBox(-2.5F, 11.5F, -10, 10, 0, 22);
        rightLeg.texOffs(0, 16 + 22);
        rightLeg.addBox(-10 + 2.5F, 11.5F, -10, 10, 0, 22);
        return createLegs(0.5F, leftLeg, rightLeg);
    }
}
