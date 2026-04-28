package su.terrafirmagreg.core.client.wearable;

import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Head attachment mesh for snorkel.
 */
public class TFGWearableHeadModel extends HumanoidModel<LivingEntity> {

    public TFGWearableHeadModel(ModelPart part, Function<ResourceLocation, RenderType> renderType) {
        super(part, renderType);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of();
    }

    public static MeshDefinition createEmptyHat(CubeListBuilder head) {
        MeshDefinition mesh = createMesh(CubeDeformation.NONE, 0);
        mesh.getRoot().addOrReplaceChild("head", head, PartPose.ZERO);
        return mesh;
    }

    public static MeshDefinition createHat(CubeListBuilder head) {
        CubeDeformation deformation = new CubeDeformation(0.51F);
        head.texOffs(0, 0);
        head.addBox(-4, -8, -4, 8, 8, 8, deformation);
        return createEmptyHat(head);
    }

    public static MeshDefinition createDiagonalHat(CubeListBuilder head, CubeListBuilder diagonalParts, String partName) {
        MeshDefinition mesh = createHat(head);
        mesh.getRoot().getChild("head").addOrReplaceChild(
                partName,
                diagonalParts,
                PartPose.rotation(45 * (float) Math.PI / 180, 0, 0));
        return mesh;
    }

    public static MeshDefinition createSnorkel() {
        CubeListBuilder head = CubeListBuilder.create();
        CubeListBuilder tube = CubeListBuilder.create();
        head.texOffs(32, 0);
        head.addBox(-6, -1.5F, -6, 8, 2, 2);
        tube.texOffs(0, 16);
        tube.addBox(-6.01F, -5, -3, 2, 2, 12);
        return createDiagonalHat(head, tube, "tube");
    }
}
