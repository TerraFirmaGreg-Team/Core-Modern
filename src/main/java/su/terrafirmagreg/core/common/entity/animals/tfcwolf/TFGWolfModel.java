package su.terrafirmagreg.core.common.entity.animals.tfcwolf;

import net.dries007.tfc.client.model.entity.HierarchicalAnimatedModel;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.common.entities.ai.predator.PackPredator;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

public class TFGWolfModel extends HierarchicalAnimatedModel<PackPredator> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "wolf"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private final ModelPart mane;
    public static final AnimationDefinition WOLF_SLEEPING;
    public static final AnimationDefinition WOLF_WALK;
    public static final AnimationDefinition WOLF_RUN;
    public static final AnimationDefinition WOLF_ATTACK;
    public static final AnimationDefinition WOLF_SWIM;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(16, 14)
                        .addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 10)
                        .addBox(-0.5F, -0.02F, -5.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 13.5F, -7.0F));
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 14).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, 1.5708F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(21, 0).addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 14.0F, -2.0F, 1.5708F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 16.0F, 7.0F));
        partdefinition.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 16.0F, 7.0F));
        partdefinition.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 16.0F, -4.0F));
        partdefinition.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 16.0F, -4.0F));
        partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(9, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 12.0F, 9.0F, 0.48F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public TFGWolfModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.mane = root.getChild("mane");
        this.rightHindLeg = root.getChild("leg1");
        this.leftHindLeg = root.getChild("leg2");
        this.rightFrontLeg = root.getChild("leg3");
        this.leftFrontLeg = root.getChild("leg4");
        this.tail = root.getChild("tail");
    }

    public void setupAnim(PackPredator entity, float limbSwing, float limbSwingAmount, float ageInTicks, float yaw, float pitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, yaw, pitch);
        if (entity.isSleeping() && entity.sleepingAnimation.isStarted()) {
            this.animate(entity.sleepingAnimation, WOLF_SLEEPING, ageInTicks);
        } else {
            if (entity.isInWaterOrBubble()) {
                this.animateWalk(WOLF_SWIM, limbSwing, limbSwingAmount, 4.0F, 2.5F);
            } else {
                if (entity.isAggressive() && EntityHelpers.isMovingOnLand(entity)) {
                    this.animateWalk(WOLF_RUN, limbSwing, limbSwingAmount, 1.0F, 2.5F);
                } else {
                    this.animateWalk(WOLF_WALK, limbSwing, limbSwingAmount, 2.5F, 2.5F);
                }

                this.animate(entity.attackingAnimation, WOLF_ATTACK, ageInTicks);
            }

            this.head.xRot = pitch * ((float) Math.PI / 180F);
            this.head.yRot = yaw * ((float) Math.PI / 180F);
        }

    }

    static {
        WOLF_SLEEPING = AnimationDefinition.Builder.withLength(2.5F).looping()
                .addAnimation("body",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(-3.25F, -6.5F, -1.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.posVec(-3.5F, -6.0F, -1.75F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.posVec(-3.25F, -6.5F, -1.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.posVec(-3.25F, -6.5F, -1.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.posVec(-3.25F, -6.5F, -1.5F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("body",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -60.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-30.09F, -4.33F, -57.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -60.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -60.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -60.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("mane",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(-4.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.posVec(-4.0F, -6.25F, 0.75F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.posVec(-4.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.posVec(-4.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.posVec(-4.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("mane",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("tail",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(-1.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.posVec(-1.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.posVec(-1.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.0F, KeyframeAnimations.posVec(-1.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.5F, KeyframeAnimations.posVec(-1.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("tail",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -72.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -72.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -72.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -90.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -72.5F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("head",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(-3.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.posVec(-3.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.posVec(-3.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.posVec(-3.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.5F, KeyframeAnimations.posVec(-3.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("head",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -42.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, -37.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.degreeVec(0.0F, -42.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.degreeVec(0.0F, -42.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.5F, KeyframeAnimations.degreeVec(0.0F, -42.5F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg1",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(1.25F, -8.5F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.posVec(1.25F, -8.5F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.posVec(1.25F, -8.5F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.posVec(1.25F, -8.5F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.posVec(1.25F, -8.5F, -2.5F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg1",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(-69.42F, -4.74F, -89.74F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-69.5F, -0.06F, -87.98F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.degreeVec(-69.42F, -4.74F, -89.74F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.degreeVec(-69.42F, -4.74F, -89.74F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.degreeVec(-69.42F, -4.74F, -89.74F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg2",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(1.25F, -6.5F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.posVec(1.25F, -6.5F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.posVec(1.25F, -6.5F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.posVec(1.25F, -6.5F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.posVec(1.25F, -6.5F, -2.5F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg2",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(-52.08F, 7.92F, -66.37F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-52.26F, 5.94F, -67.92F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.degreeVec(-52.08F, 7.92F, -66.37F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.degreeVec(-52.08F, 7.92F, -66.37F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.degreeVec(-52.08F, 7.92F, -66.37F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg3",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(-3.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.9583434F, KeyframeAnimations.posVec(-3.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.posVec(-3.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.posVec(-3.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.posVec(-3.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg3",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(-75.0F, 0.0F, -87.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.9583434F, KeyframeAnimations.degreeVec(-75.0F, 0.0F, -87.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.degreeVec(-75.0F, 0.0F, -87.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.degreeVec(-75.0F, 0.0F, -87.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.degreeVec(-75.0F, 0.0F, -87.5F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg4",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(-3.75F, -7.0F, 1.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.9583434F, KeyframeAnimations.posVec(-3.75F, -7.0F, 1.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.posVec(-3.75F, -7.0F, 1.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.posVec(-3.75F, -7.0F, 1.5F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.posVec(-3.75F, -7.0F, 1.5F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg4",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(-86.51F, -29.37F, 0.86F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.9583434F, KeyframeAnimations.degreeVec(-86.51F, -29.37F, 0.86F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.7916766F, KeyframeAnimations.degreeVec(-86.51F, -29.37F, 0.86F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.9167666F, KeyframeAnimations.degreeVec(-86.51F, -29.37F, 0.86F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(2.1676664F, KeyframeAnimations.degreeVec(-86.51F, -29.37F, 0.86F), AnimationChannel.Interpolations.LINEAR) }))
                .build();
        WOLF_WALK = AnimationDefinition.Builder.withLength(1.0F).looping()
                .addAnimation("leg1",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg2",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg3",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg4",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("tail",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .build();
        WOLF_RUN = AnimationDefinition.Builder.withLength(0.5F).looping()
                .addAnimation("head",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.3433333F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("body",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.3433333F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("body",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.3433333F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("mane",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.3433333F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg1",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.08343333F, KeyframeAnimations.degreeVec(42.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.3433333F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg2",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.125F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.3433333F, KeyframeAnimations.degreeVec(-34.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg3",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.125F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.3433333F, KeyframeAnimations.degreeVec(33.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg4",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.08343333F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.3433333F, KeyframeAnimations.degreeVec(36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("tail",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.3433333F, KeyframeAnimations.posVec(0.0F, -1.75F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("tail",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(55.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.125F, KeyframeAnimations.degreeVec(77.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.3433333F, KeyframeAnimations.degreeVec(51.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(55.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .build();
        WOLF_ATTACK = AnimationDefinition.Builder.withLength(0.375F)
                .addAnimation("head",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -0.25F, -0.75F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("head",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-6.93F, 2.86F, 39.83F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("body",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.5F, -1.75F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("body",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("mane",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("mane",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg1",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg2",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg3",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg4",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("tail",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("tail",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(75.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .build();
        WOLF_SWIM = AnimationDefinition.Builder.withLength(0.5F).looping()
                .addAnimation("head",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("body",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -7.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -7.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -7.0F, -2.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("body",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(-37.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-37.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("mane",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("mane",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg1",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg1",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg2",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -10.0F, -3.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg2",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg3",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg3",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(-92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg4",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("leg4",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(-55.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-55.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("tail",
                        new AnimationChannel(AnimationChannel.Targets.POSITION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -12.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -12.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -12.0F, -2.0F), AnimationChannel.Interpolations.LINEAR) }))
                .addAnimation("tail",
                        new AnimationChannel(AnimationChannel.Targets.ROTATION,
                                new Keyframe[] { new Keyframe(0.0F, KeyframeAnimations.degreeVec(100.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(87.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(100.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR) }))
                .build();
    }
}
