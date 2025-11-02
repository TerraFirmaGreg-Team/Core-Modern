package su.terrafirmagreg.core.common.data.entities.astikorcarts;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

import de.mennomax.astikorcarts.client.renderer.entity.model.CartModel;
import de.mennomax.astikorcarts.client.renderer.entity.model.EasyMeshBuilder;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;

public final class RNRPlowModel extends CartModel<RNRPlow> {
    @SuppressWarnings({ "removal" })
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(TFGCore.MOD_ID + ":rnr_plow"), "main");

    private final ModelPart[] plowShaftUpper = new ModelPart[3];
    private final ModelPart[] plowShaftLower = new ModelPart[3];

    @Getter
    private final ModelPart axis;
    @Getter
    private final ModelPart shaftsGroup;
    @Getter
    private final ModelPart triangle0;
    @Getter
    private final ModelPart triangle1;

    public RNRPlowModel(final ModelPart root) {
        super(root);
        final ModelPart body = root.getChild("body");
        this.axis = body.getChild("axis");
        ModelPart partsGroup = body.getChild("parts");
        this.shaftsGroup = partsGroup.getChild("shafts");
        this.triangle0 = partsGroup.getChild("triangle_0");
        this.triangle1 = partsGroup.getChild("triangle_1");

        for (int i = 0; i < this.plowShaftUpper.length; i++) {
            this.plowShaftUpper[i] = partsGroup.getChild("plow_shaft_upper_" + i);
            this.plowShaftLower[i] = this.plowShaftUpper[i].getChild("plow_shaft_lower_" + i);
        }
    }

    public ModelPart getUpperShaft(final int i) {
        return this.plowShaftUpper[i];
    }

    @Override
    public void setupAnim(final @NotNull RNRPlow entity, final float delta, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float pitch) {
        super.setupAnim(entity, delta, limbSwingAmount, ageInTicks, netHeadYaw, pitch);

        for (final ModelPart upper : this.plowShaftUpper) {
            upper.xRot = (float) (entity.getPlowing() ? Math.PI / 4.0D - Math.toRadians(pitch) : Math.PI / 2.5D);
        }

        // Hide all base plow geometry so DrawnRenderer's default pass renders none of it.
        this.axis.visible = false;
        this.triangle0.visible = false;
        this.triangle1.visible = false;
        this.shaftsGroup.visible = false;
        for (final ModelPart upper : this.plowShaftUpper) {
            upper.visible = false;
        }
    }

    public static LayerDefinition createLayer() {
        final MeshDefinition def = CartModel.createDefinition();

        final EasyMeshBuilder axis = new EasyMeshBuilder("axis", 0, 0);
        axis.addBox(-12.5F, -1.0F, -1.0F, 25, 2, 2);

        final EasyMeshBuilder[] triangle = new EasyMeshBuilder[3];
        triangle[0] = new EasyMeshBuilder("triangle_0", 0, 4);
        triangle[0].addBox(-7.5F, -5.0F, -10.0F, 15, 4, 22);

        triangle[1] = new EasyMeshBuilder("triangle_1", 0, 11);
        triangle[1].addBox(-6.5F, -3.0F, -9.0F, 13, 13, 20);

        final EasyMeshBuilder shaft = new EasyMeshBuilder("shaft", 0, 8);
        shaft.zRot = -0.07F;
        shaft.addBox(0.0F, 0.0F, -8.0F, 20, 2, 1);
        shaft.addBox(0.0F, 0.0F, 7.0F, 20, 2, 1);

        final EasyMeshBuilder shaftConnector = new EasyMeshBuilder("shaftConnector", 0, 27);
        shaftConnector.zRot = -0.26F;
        shaftConnector.addBox(-16.0F, 0.0F, -8.0F, 16, 2, 1);
        shaftConnector.addBox(-16.0F, 0.0F, 7.0F, 16, 2, 1);

        final EasyMeshBuilder shafts = new EasyMeshBuilder("shafts");
        shafts.setRotationPoint(0.0F, 0.0F, -14.0F);
        shafts.yRot = (float) Math.PI / 2.0F;
        shafts.addChild(shaft);
        shafts.addChild(shaftConnector);

        final EasyMeshBuilder[] plowShaftUpper = new EasyMeshBuilder[3];
        final EasyMeshBuilder[] plowShaftLower = new EasyMeshBuilder[3];
        for (int i = 0; i < plowShaftUpper.length; i++) {
            plowShaftUpper[i] = new EasyMeshBuilder("plow_shaft_upper_" + i, 56, 0);
            plowShaftUpper[i].addBox(-1.0F, 2.0F, -2.0F, 1, 20, 6);
            plowShaftUpper[i].setRotationPoint(-3.0F + 3.5F * i, -1.0F, 0.0F);
            plowShaftUpper[i].yRot = -0.523599F + (float) Math.PI / 6.0F * i;

            // Just making these size 0 since it's too much work to remove them :3
            plowShaftLower[i] = new EasyMeshBuilder("plow_shaft_lower_" + i, 42, 4);
            plowShaftLower[i].addBox(-1.0F, -0.7F, -0.7F, 0, 0, 0);
            plowShaftLower[i].setRotationPoint(0.0F, 28.0F, -1.0F);
            plowShaftLower[i].xRot = (float) Math.PI / 4.0F;
            plowShaftUpper[i].addChild(plowShaftLower[i]);
        }

        final EasyMeshBuilder parts = new EasyMeshBuilder("parts");
        parts.setRotationPoint(0.0F, -5.0F, -1.0F);
        parts.addChild(shafts);
        parts.addChild(triangle[0]);
        parts.addChild(triangle[1]);
        parts.addChild(plowShaftUpper[0]);
        parts.addChild(plowShaftUpper[1]);
        parts.addChild(plowShaftUpper[2]);

        final EasyMeshBuilder body = CartModel.createBody();
        body.addChild(axis);
        body.addChild(parts);
        body.build(def.getRoot());

        return LayerDefinition.create(def, 64, 64);
    }
}
