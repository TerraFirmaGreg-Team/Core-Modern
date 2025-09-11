package su.terrafirmagreg.core.common.data.entities.wraptor;

import com.ninni.species.client.animation.WraptorAnimations;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class TFCWraptorModel<E extends TFCWraptor> extends HierarchicalModel<E> {

    private final ModelPart root;
    private final ModelPart all;
    private final ModelPart leftLeg;
    private final ModelPart leftKnee;
    private final ModelPart leftFoot;
    private final ModelPart rightLeg;
    private final ModelPart rightKnee;
    private final ModelPart rightFoot;
    private final ModelPart body;
    private final ModelPart bodyFeathers;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart tail;
    private final ModelPart tailMid;
    private final ModelPart tailTip;
    private final ModelPart tailFeathers;
    private final ModelPart neck;
    private final ModelPart neckFeathersTop;
    private final ModelPart neckFeathersMid;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart headFeathers;
    private final ModelPart hairTuft;
    private final ModelPart topFrill;
    private final ModelPart leftFrill;
    private final ModelPart rightFrill;
    private final ModelPart neckFeathersBottom;

    public TFCWraptorModel(ModelPart root) {
        this.root = root;
        this.all = root.getChild("all");
        this.leftLeg = this.all.getChild("leftLeg");
        this.leftKnee = this.leftLeg.getChild("leftKnee");
        this.leftFoot = this.leftKnee.getChild("leftFoot");
        this.rightLeg = this.all.getChild("rightLeg");
        this.rightKnee = this.rightLeg.getChild("rightKnee");
        this.rightFoot = this.rightKnee.getChild("rightFoot");
        this.body = this.all.getChild("body");
        this.bodyFeathers = this.body.getChild("bodyFeathers");
        this.rightWing = this.body.getChild("rightWing");
        this.leftWing = this.body.getChild("leftWing");
        this.tail = this.body.getChild("tail");
        this.tailMid = this.tail.getChild("tailMid");
        this.tailTip = this.tailMid.getChild("tailTip");
        this.tailFeathers = this.tailTip.getChild("tailFeathers");
        this.neck = this.body.getChild("neck");
        this.neckFeathersTop = this.neck.getChild("neckFeathersTop");
        this.neckFeathersMid = this.neck.getChild("neckFeathersMid");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.headFeathers = this.head.getChild("headFeathers");
        this.hairTuft = this.head.getChild("hairTuft");
        this.topFrill = this.head.getChild("topFrill");
        this.leftFrill = this.head.getChild("leftFrill");
        this.rightFrill = this.head.getChild("rightFrill");
        this.neckFeathersBottom = this.neck.getChild("neckFeathersBottom");
    }

    public void setupAnim(E entity, float limbAngle, float limbDistance, float animationProgress, float headYaw,
            float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        int stage = entity.getFeatherStage();
        float speed = 1.5F;
        float degree = 1.0F;
        float pi = (float) Math.PI;
        ModelPart var10000 = this.head;
        var10000.xRot += headPitch * (pi / 180.0F) - headPitch * (pi / 180.0F) / 2.0F;
        var10000 = this.head;
        var10000.yRot += headYaw * (pi / 180.0F) - headYaw * (pi / 180.0F) / 2.0F;
        var10000 = this.neck;
        var10000.xRot += headPitch * (pi / 180.0F) / 2.0F;
        var10000 = this.neck;
        var10000.yRot += headYaw * (pi / 180.0F) / 2.0F;
        if (this.young) {
            this.applyStatic(WraptorAnimations.BABY_PROPORTIONS);
        }

        this.animateWalk(WraptorAnimations.WALK, limbAngle, limbDistance, 2.7F, 100.0F);
        this.animate(entity.roarAnimationState, WraptorAnimations.ROAR, animationProgress);
        this.animate(entity.fallingAnimationState, WraptorAnimations.FALLING, animationProgress);
        this.headFeathers.visible = stage >= 5 && !this.young;
        this.hairTuft.visible = stage >= 5 || this.young;
        this.neckFeathersTop.visible = stage >= 4 && !this.young;
        this.neckFeathersMid.visible = stage >= 3 && !this.young;
        this.neckFeathersBottom.visible = stage >= 2 && !this.young;
        this.bodyFeathers.visible = stage >= 1 && !this.young;
        this.rightFrill.visible = stage <= 4 || this.young;
        this.leftFrill.visible = stage <= 4 || this.young;
        this.topFrill.visible = stage <= 4 && !this.young;
        // this.leftWing.visible = stage == 0 || this.young;
        // this.rightWing.visible = stage == 0 || this.young;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition all = partdefinition.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition leftLeg = all.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F,
                0.0F, 0.0F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -12.0F, 3.0F));
        PartDefinition leftKnee = leftLeg.addOrReplaceChild("leftKnee", CubeListBuilder.create().texOffs(4, 19).addBox(
                -1.0F, 0.0F, 0.0F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));
        leftKnee.addOrReplaceChild("leftFoot", CubeListBuilder.create().texOffs(-9, 0).addBox(-2.5F, 0.0F, -6.0F, 5.0F,
                0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition rightLeg = all.addOrReplaceChild("rightLeg",
                CubeListBuilder.create().texOffs(0, 19).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(-3.0F, -12.0F, 3.0F));
        PartDefinition rightKnee = rightLeg.addOrReplaceChild("rightKnee",
                CubeListBuilder.create().texOffs(4, 19).mirror()
                        .addBox(-1.0F, 0.0F, 0.0F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        rightKnee.addOrReplaceChild("rightFoot",
                CubeListBuilder.create().texOffs(-9, 0).mirror()
                        .addBox(-2.5F, 0.0F, -6.0F, 5.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F,
                -7.5F, 8.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, 3.5F));
        body.addOrReplaceChild("bodyFeathers", CubeListBuilder.create().texOffs(0, 19).addBox(-4.0F, -5.5F, -5.5F, 8.0F,
                11.0F, 11.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -1.5F, -2.0F));
        PartDefinition rightWing = body.addOrReplaceChild("rightWing", CubeListBuilder.create(),
                PartPose.offset(-4.0F, -7.0F, -6.5F));
        rightWing.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(-17, 63).mirror()
                        .addBox(-30.0F, 0.0F, -5.0F, 30.0F, 0.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, -1.0472F));
        PartDefinition leftWing = body.addOrReplaceChild("leftWing", CubeListBuilder.create(),
                PartPose.offset(4.0F, -7.0F, -6.5F));
        leftWing.addOrReplaceChild("cube_r2",
                CubeListBuilder.create().texOffs(-17, 63).addBox(0.0F, 0.0F, -5.0F, 30.0F, 0.0F, 17.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 1.0472F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(28, 38).addBox(-2.0F,
                -2.5F, 0.0F, 4.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.5F, 3.5F));
        PartDefinition tailMid = tail.addOrReplaceChild("tailMid", CubeListBuilder.create().texOffs(25, 41)
                .addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.01F)),
                PartPose.offset(0.0F, -2.5F, 11.0F));
        PartDefinition tailTip = tailMid.addOrReplaceChild("tailTip", CubeListBuilder.create().texOffs(38, 28).addBox(
                -1.5F, -1.5F, -6.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.5F, -2.0F));
        PartDefinition tailFeathers = tailTip.addOrReplaceChild("tailFeathers", CubeListBuilder.create(),
                PartPose.offset(0.0F, -1.5F, -6.0F));
        tailFeathers
                .addOrReplaceChild("cube_r3",
                        CubeListBuilder.create().texOffs(41, 0).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 0.0F, 9.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(38, 0).addBox(-1.5F,
                -13.0F, -1.5F, 3.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, -6.0F));
        neck.addOrReplaceChild("neckFeathersTop", CubeListBuilder.create().texOffs(56, 28).addBox(-2.5F, -2.0F, -2.5F,
                5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));
        neck.addOrReplaceChild("neckFeathersMid", CubeListBuilder.create().texOffs(60, 57).addBox(-2.5F, -2.0F, -2.5F,
                5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.5F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(48, 11)
                        .addBox(-3.0F, -5.0F, -2.5F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(32, 16)
                        .addBox(-2.5F, -2.0F, -8.5F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(48, 21)
                        .addBox(-2.5F, 0.0F, -8.5F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -13.0F, 0.0F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(55, 49).addBox(-2.5F, -1.0F, -7.0F, 5.0F, 1.0F,
                7.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, -1.25F));
        head.addOrReplaceChild("headFeathers", CubeListBuilder.create().texOffs(0, 41).addBox(-3.5F, -3.0F, -2.5F, 7.0F,
                6.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition hairTuft = head.addOrReplaceChild("hairTuft", CubeListBuilder.create().texOffs(0, 45).addBox(
                0.0F, -5.0F, -3.5F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, 0.0F));
        hairTuft.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 50).addBox(0.0F, 0.5F, -3.5F, 0.0F,
                3.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.0F, 1.5708F, 0.0F));
        PartDefinition topFrill = head.addOrReplaceChild("topFrill", CubeListBuilder.create(),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        topFrill.addOrReplaceChild("cube_r5",
                CubeListBuilder.create().texOffs(49, 37).addBox(-4.0F, -5.5F, 0.0F, 8.0F, 11.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 1.5708F, 0.0F, -1.5708F));
        head.addOrReplaceChild("leftFrill", CubeListBuilder.create().texOffs(49, 37).addBox(0.0F, -5.5F, 0.0F, 8.0F,
                11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -2.5F, 0.5F));
        head.addOrReplaceChild("rightFrill",
                CubeListBuilder.create().texOffs(49, 37).mirror()
                        .addBox(-8.0F, -5.5F, 0.0F, 8.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(-3.0F, -2.5F, 0.5F));
        neck.addOrReplaceChild("neckFeathersBottom", CubeListBuilder.create().texOffs(60, 66).addBox(-6.0F, -15.0F,
                -4.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 12.0F, 1.5F));
        return LayerDefinition.create(meshdefinition, 80, 80);
    }

    public ModelPart root() {
        return this.root;
    }
}
